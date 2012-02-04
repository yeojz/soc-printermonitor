/*
 * JS for Printer Monitor Desktop
 * Full Version
 */
		
	
/*
 ******************************
 *
 * 	Document Ready!
 *
 ******************************
 */

$(document).ready(function(){

	var firstInit 	= true;		//check first initialisation
	var PM_URL		= 'plug.html';
	var appName 	= '';//<h1 id="page-name"><a href="'+PM_URL+'">Printer Monitor</a></h1>';
	var appLogo		= '<img class="logo" src="images/pm.png" alt="" />';
	var scrollQueue = new Array();		//start queue
	var scrollQueue2 = new Array();		//end queue
	var scrollT;	//scroll timeout
	var scrollT_on = false;
	var updateT;	//update timeout
	var updateT_on = true;
	var min_width_target = 600; //min screen width
	
	
/*
 ******************************
 *
 * 		Query Modifiers
 *
 ******************************
 */	
	
	//printer list
	var enableGroup;	
	var printerGET = $.query.get('p');
	
	if (  printerGET ){
		enabledGroup = printerGET;
	}else{
		enabledGroup = ['psts', 'pstsb', 'pstsc', 'psc008', 'psc011', 'psc245']; //enabled printer group
	}
	
	var pluglocation = $.query.get('loc');
	if (  pluglocation == 1){
		enabledGroup = ['psts', 'pstsb', 'pstsc']; //enabled printer group
	}else if (  pluglocation == 2){
		enabledGroup = ['psc008', 'psc011']; //enabled printer group
		
	}
	
	
	//font size
	if ( $.query.get('fsize') ){
		$("#wrapper").css("font-size", $.query.get('fsize')); 
	}
	


	
/*
 ******************************
 *
 * 		Page Components
 *
 ******************************
 */
	
	//inititalize page
	function initStructure(){
		//set page details
		//$("header.main").html(appName);
		$("header.main").html(appLogo + appName);
	}
	
	
	//adds printer to navigation list
	function addPrinter(item){
		if ($.inArray(item.group, enabledGroup) < 0)
			return;
			
		$("#printer-list ul").append('<li><a id="nav-'+item.group+'" href="#list-'+item.group+'">'+item.group+'<span class="ui-li-count">'+item.total+'</span></a></li>');
		addSection(item.group);
		
		//PUSH!
		scrollQueue.push(item.group);
	}

	//update count in main nav
	function updateCountQueue(nav, count){
		$(nav+" .ui-li-count").html(count);
	}
	
	
	//adds printer section div
	function addSection(name){
		$("#queue-display").append('<div id="list-'+name+'" class="listQ hide"></div>');
	}

	//adds Sub Queues of Printers into sections
	function addSubQueue(group, item){
		if ($.inArray(group, enabledGroup) < 0)
			return;
	
		var group_id = "list-"+group;
		var queue_id = "subQ-"+item.pname;
		$("#"+group_id).append('<section id="'+queue_id+'" class="subQ"><header><h2>'+item.pname+'</h2><span class="ui-li-count">'+item.count+'</span></header></section>')

		if (item.count){
			addSubQueueDocuments(queue_id, item.queue);
		};
	}
	
	//updates Sub Queues of Printers into sections
	function updateSubQueue(group, item){
		addSubQueue(group, item);
	}
	
	
	//adds the Document list of sub queues
	function addSubQueueDocuments(queue_id, queue){
		//iterate through the list
		$.each(queue, function(index, field) {
			var size = Math.round(field.size/10.24)/100;
			$("#"+queue_id).append('<section class="job"><ul>	<li class="matric"><strong>'+field.owner+'</strong></li>	<li class="size"><strong>'+size+'</strong> KB</li>	<li class="jobID">Job ID: <strong>'+field.id+'</strong></li>	</ul><div class="clear"></div></section>');
		})
		$("#"+queue_id).append('<div class="clear"></div>');
	}
	

	
/*
 ******************************
 *
 * 	Core
 *
 ******************************
 */

	/*
 	 ******************************
 	 * 		Initialization
 	 ******************************
 	 */

	function initDoc(data){
		$.each(data.printmonitor, function(key, item) {
			
			//add list of printers
			addPrinter(item);

			//Sub queues
			$.each(item.types, function(sub_key, sub_item) {
				addSubQueue(item.group, sub_item);
			})
			

		});
		$("#printer-list").append('<div class="clear"></div>');
		$("#listQ").append('<div class="clear"></div>');
		firstInit = false;
	}


	/*
 	 ******************************
 	 * 		Update Only
 	 ******************************
 	 */
	function refreshDoc(data){
	
		//clear the queue
		//clearSubQueueDocuments(true, 1);
		
		$.each(data.printmonitor, function(key, item) {
			
			//update main nav queue count
			updateCountQueue('#nav-'+item.group, item.total);
			
			//Sub queues
			//$('#list-'+item.group+' .subQ').remove();
			//$('#list-'+item.group+' div.clear').remove();
		
			$('#list-'+item.group).html(" ");
			$.each(item.types, function(sub_key, sub_item) {
				updateSubQueue(item.group, sub_item);
			})
		});
		$(".listQ").append('<div class="clear"></div>');
	
	}


/*
 ******************************
 *
 * 		Main Wrapper Calls
 *
 ******************************
 */


	/*
 	 ******************************
 	 * 		AJAX CALL
 	 ******************************
 	 */	
	function getQueue(){
		$.ajax({
			url: 'summary.txt',
			//dataType: 'json',
		 	success: function(data) {
				if (firstInit){
					initDoc( $.parseJSON(data) );
					
					//set home
					//scrollQueue.push('home');
				}else{
					refreshDoc( $.parseJSON(data) );	
				}
		  	}
		});
	}


	
	
/*
 ******************************
 *
 * 	User Interaction
 *
 ******************************
 */

 	 /*
 	 ******************************
 	 * 		Navi State
 	 ******************************
 	 */
	function showList(listName){
		$('.show').hide().addClass('hide').removeClass('show');
		$(listName).show().addClass('show').removeClass('hide');
	}
 
 
 
	$('#printer-list ul li a').live('click', function() {
		showList( $(this).attr('href') );
		$('#printer-list ul li a.active').removeClass('active');
		$(this).addClass('active');
		if ( $(window).width() < min_width_target ){
			$('#printer-list ul').hide();
		}
		return false;
	});


	
	/*
 	 ******************************
 	 * 		Resize Target
 	 ******************************
 	 */
	$('#printer-list h2.mlink').live('click', function() {
		var status = $('#printer-list ul').css("display");
		if ( status == 'none'){
			$('#printer-list ul').hide();
		} else {
			$('#printer-list ul').show();
		}
		
	});

	$(window).resize(function() {
		if ( $(window).width() > min_width_target ){
			$('#printer-list ul').show();
		}else{
			//$('#printer-list ul').hide();
		}
	});
	
	 /*
 	 ******************************
 	 * 		Start Timer
 	 ******************************
 	 */
	function updateTimer(time){
		updateT = setTimeout(function(){
            getQueue();
			updateT_on = true;
			updateTimer(10000);
        }, time);
		
	}
	
	function scrollTimer(time, start, end){
		scrollT = setTimeout(function(){
			if (end.length == 0){
				start.reverse();
			}
			
			if (start.length != 0){
				var name = start.pop();
				end.push(name);
				$('#printer-list ul li a.active').removeClass('active');
				$('#nav-'+name).addClass('active');
				showList('#list-'+name);
				
				scrollTimer(time, start, end);
			}else{
				scrollTimer(time, end, start);
			}
			
			scrollT_on = true;
		 }, time);
	}
	
	
	function clearTheTimer(currTimer){
		clearTimeout(currTimer);
	}
	
	 
	$('a#liveScroll').live('click', function() {
		if (scrollT_on){
			clearTheTimer(scrollT);
			scrollT_on = false;
			$("a#liveScroll").html('Queue Scrolling <strong>(Disabled)</strong>');
		}else{
			scrollTimer(5000, scrollQueue, scrollQueue2);
			scrollT_on = true;
			$("a#liveScroll").html('Queue Scrolling <strong class="ok">(Enabled)</strong>');
		}
		return false;
	});

	$('a#liveUpdate').live('click', function() {
		if (updateT_on){
			clearTheTimer(updateT);
			updateT_on = false;
			$("a#liveUpdate").html('Live Queue Update <strong>(Disabled)</strong>');
		}else{
			getQueue();
			updateTimer(10000);
			updateT_on = true;
			$("a#liveUpdate").html('Live Queue Update <strong class="ok">(Enabled)</strong>');
		}
		return false;
	});
	
	
/*
 ******************************
 *
 * 	Ready, Get Set, GO!!
 *
 ******************************
 */
	initStructure();
	getQueue();
	updateTimer(10000);
	
	
	//if ( $.query.get('qscroll') ){
	scrollTimer(5000, scrollQueue, scrollQueue2);
	scrollT_on = true;

	//}	
	//scrollTimer(5000, scrollQueue, scrollQueue2);
});


