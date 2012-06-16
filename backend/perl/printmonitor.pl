#!/usr/bin/perl

# PrintMonitor - adapted from the Java version originally by hirman and modified by yeojz
# config file format:
#    line 1: summary output file name
#    line 2: number of minutes to run
#    line 3: sleep tme
#    lines 4-N: printerName printerQueueName1 printerQueueName2 ...
#
#
# Summary output format (JSON) (for each printer):
#	{group: psts, total: 0, types: [
#		{name: psts, count: 0, queue: [
#			{id: 0, file: this, size: 0}
#		]},
#		{name: psts-dx, count: 0, queue: [
#			{id: 0, file: this, size: 0}
#		]},
#	]}
#
#
# Warning: Perl n00b code ahead.
#          "Abandon hope, all ye who enter here"

use strict;
use Time::HiRes qw(gettimeofday usleep);

my ($outputFile, $runMinutes, $sleepTime);
my @printerNames;   # To preserve the order given in the config file
my %printers = ();  # printers and their queues; map from printerName to info about the printer:
  # printerName1 => (	numJobs => total number of jobs for this printer
  #			queues  => [queue1, queue2, ... queueN] == queues for this printer
  #			queue1  => [(owner: owner1, id: job1, size: filesize1), (owner: owner2, ...) ... ]
  #					== array of jobs
  #		);
  # printerName2 => ... 
  # ...

my ($endTime, $now, $startTime);


# Read the config file
open CONFIGFILE, 'config-siglabs.txt' or die $!;

chomp($outputFile = <CONFIGFILE>);
chomp($runMinutes = <CONFIGFILE>);
chomp($sleepTime  = <CONFIGFILE>);
$sleepTime *= 1000; # Sleep time in ms, but usleep uses microseconds

while (chomp(my $line = <CONFIGFILE>)) {
	my @queues = split(' ', $line);
	if (scalar @queues > 0) {
		my $printerName = $queues[0];
		push(@printerNames, $printerName);
		$printers{ $printerName }{'queues'} = [@queues];
	}
}
close CONFIGFILE;
# Done reading the config file


# Init the time vars
sub getTime;
$now = $startTime = getTime;
$endTime = $startTime + $runMinutes * 60000;
# Done init


# Update loop
do {
	# Update status
	foreach my $printer (@printerNames) {
		my $jobCount = 0;
		foreach my $queue (@{ $printers{ $printer }{'queues'} }) {
			(my @output = `/usr/local/bin/lpq -P$queue`);

			# Clear out the original array
			$printers{$printer}{$queue} = [];

			# Skip till the line that contains 'Rank blah blah'
			my $line;
			while (defined($line = shift(@output)) && $line !~ /^Rank/) { }

			# Now log the details (owner, ID, size)
			while (defined($line = shift(@output))) {
				my @details = split(/\s+/, $line);
				my $owner   = $details[1];
				my $id      = $details[2];
				my $size    = $details[scalar @details - 2];

				my %job = ('id'=>$id, 'owner'=>$owner, 'size'=>$size);
				push(@{$printers{$printer}{$queue}}, \%job);

				++$jobCount;
			}
		}
		$printers{$printer}{'numJobs'} = $jobCount;
	}
	# Done updating


	# Write to output file (in JSON)
	open OUTPUTFILE, ">$outputFile" or die $!;

	sub jsonPrinterQueue;
	my $jsonResult = '{"printmonitor" : [';
	foreach my $printer (@printerNames) {
		$jsonResult .=  '{"group": "' . $printer .
				'","total": "' . $printers{$printer}{'numJobs'} .
				'","types" : [';

		# Handle individual print queues
		foreach my $queue (@{ $printers{$printer}{'queues'} }) {
			$jsonResult .= jsonPrinterQueue $queue, \@{$printers{$printer}{$queue}};
		}
		$jsonResult = substr($jsonResult, 0, -1);

		$jsonResult .= ']},';
	}
	$jsonResult = substr($jsonResult, 0, -1);

	my $timestamp =  sprintf "%.0f", getTime;

	$jsonResult .= "],\"time\" : \"$timestamp\"}";

	print OUTPUTFILE $jsonResult;
	close OUTPUTFILE;
	# Done writing


	# Get some sleep
	usleep($sleepTime);
	$now = getTime;
} while ($now < $endTime);


# Subroutine for formatting the JSON for individual queues
sub jsonPrinterQueue
{
	my $queue = $_[0];
	my $jobs  = $_[1];

	my $jsonResult = "{ \n";
	$jsonResult .= '"pname": "' . $queue . '" ,"count": "' . scalar @{$jobs} . '","queue": [ ';

	foreach my $job (@{$jobs}) {
		$jsonResult .= '{"id": "'    . %{$job}->{'id'}    . '",' .
			'"owner": "' . %{$job}->{'owner'} . '",' .
				'"size": "'  . %{$job}->{'size'}   . '"},';
	}
	$jsonResult = substr($jsonResult, 0, -1);

	$jsonResult .= ']},';
}

# Subroutine to get current time in ms
sub getTime
{
	my ($seconds, $microseconds) = gettimeofday;
	return $seconds * 1000 + $microseconds / 1000;
}

# Goodbye!

