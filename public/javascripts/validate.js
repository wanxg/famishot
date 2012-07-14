
$(document).ready(function()// When the dom is ready
{
$("#username").change(function()
{ // if theres a change in the username textbox

var username = $("#username").val();// Get the value in the username textbox
if(username.length > 3)// if the lenght greater than 3 characters
	{
		$("#availability_status").html('<img src="loader.gif" align="absmiddle">&nbsp;Checking availability...');
		// Add a loading image in the span id="availability_status"
		
		$.ajax(
			{
				
				 &nbsp; // Make the Ajax Request
				 type: "POST",
				 url: "ajax_check_username.php",&nbsp; // file name
				 data: "username="+ username,&nbsp; // data
				 success: function(server_response){
				
				 $("#availability_status").ajaxComplete(function(event, request){
				
				 if(server_response == '0')// if ajax_check_username.php return value "0"
				 {
				 $("#availability_status").html('<img src="available.png" align="absmiddle"> <font color="Green"> Available </font>&nbsp; ');
				 // add this image to the span with id "availability_status"
				 }
				 else&nbsp; if(server_response == '1')// if it returns "1"
				 {
				 $("#availability_status").html('<img src="not_available.png" align="absmiddle"> <font color="red">Not Available </font>');
				 }
				
				 });
				 }
			
			 }
		);
	
	}
else
	{
	
	$("#availability_status").html('<font color="#cc0000">Username too short</font>');
	// if in case the username is less than or equal 3 characters only
	}

return false;
});
});
