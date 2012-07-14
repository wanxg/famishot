/*
 * --------------------------------------------------------------------
 * In-place editing for photo title and description  
 * --------------------------------------------------------------------
 */

$(document).ready(function() 
{
    var oldText, newText, oldAreaText, newAreaText;
    $(".photoTitle").hover(
        function()
        {    
			if($(this).children("form").length==0)
				$(this).addClass("editHover");
        }, 
        function()
        {
            $(this).removeClass("editHover");
        }
    );
    
    $(".photoDes").hover(
            function()
            {    
    			if($(this).children("form").length==0)
    				$(this).addClass("editHover");
            }, 
            function()
            {
                $(this).removeClass("editHover");
            }
    );
    
    
  
    $(".photoTitle").bind("click", replaceTitleHTML);
    
    $(".photoDes").bind("click", replaceDesHTML);
     
     
    $(".btnSave").live("click", 
                    function()
                    {
                        newText = $(this).parent().siblings("form")
                                         .children("input")
                                         .val().replace(/"/g, "&quot;");
                        
                        newAreaText = $(this).parent().siblings("form")
				                        .children("textarea")
				                        .val().replace(/"/g, "&quot;");
                        
                        if(oldAreaText==newAreaText && oldText==newText){
                        	
                        	$(".btnDiscard").click();
                        }
                                         
                        var div_id = $(this).parent().parent().attr('id');
                        
                        
                        var url = "@controllers.MainProgram.updatePhotoTitleDes()";
                        
                        url =  $("#photoTitleDes_Form").attr( 'action' );
                        
                        var title="", des="";
                        
                        $.post( url, { photoId: div_id, title: newText , des: newAreaText},
                        	      function( data ) {
                        				title= data.title;
                        				des= data.des;
                        				
	               	                     $("#" + div_id).siblings(".photoDes")
	            	                     .html(des)
	            	                     .removeClass("noArea")
	            	                     .bind("click", replaceDesHTML);
	                                    
	                                    $("#" + div_id)
	                                    .html(title)
	                                    .removeClass("noPad")
	                                    .bind("click", replaceTitleHTML);
	                                    
	                                    
	                                    $("#a_" + div_id)
	                                    	.attr('title', des);
	                                    
	                                    $("#img_" + div_id)
                                    		.attr('alt', title);
	                                    
	                                    
	                                    showPlaceHolder();
	                                    
                        	      }
                        	    );
                    }
    ); 
    
    
    $(".btnDiscard").live("click", 
                    function()
                    {
    	
    					//alert($(this).parent().parent().siblings(".photoDes").attr('class'));
                        $(this).parent().parent().siblings(".photoDes")
        				.html(oldAreaText)
        				.removeClass("noArea")
                        .bind("click", replaceDesHTML);
    					
                        $(this).parent().parent()
                               .html(oldText)
                               .removeClass("noPad")
                               .bind("click", replaceTitleHTML);
                        showPlaceHolder();				
                    }
    ); 
    
    
    function replaceTitleHTML(){
                        oldText = $(this).html()
                                         .replace(/"/g, "&quot;").replace("Click to input ...", "");
                        
                        var desDiv = $(this).siblings(".photoDes");
                        
                        oldAreaText = desDiv.html().replace(/"/g, "&quot;").replace("Click to input ...", "");
                        
						$(this).removeClass("editHover");
						
                        $(this).addClass("noPad")
                               .html("")
                               .html("<form id=\"photoTitleDes_Form\" action=\"/ajax/photo/updateTitleDes\">" +
                               		"<input id=\"input_photoTitle\" placeholder=\"Click to input ...\" autofocus=\"autofocus\" type=\"text\" class=\"span3\" value=\"" + oldText + "\" /> " +
                               		"<textarea id=\"textarea_photoDes\" placeholder=\"Click to input ...\" class=\"span3\" id=\"textarea\" rows=\"5\">" + oldAreaText+ "</textarea>" + 
                               		"</form><div class=\"inplaceButtons\"><a href=\"#\" class=\"btn btn-primary btn-mini btnSave\">Save</a> <a href=\"#\" class=\"btn btn-primary btn-mini btnDiscard\">Cancel</a></div>")
                               .unbind('click', replaceTitleHTML)
                               .unbind('click', replaceDesHTML);
                        
                        desDiv.html("")
                        	  .addClass("noArea")
                              .unbind('click', replaceTitleHTML)
                              .unbind('click', replaceDesHTML);
                        
                        $("#input_photoTitle").keypress(function(e)
                                {
                    		        code= (e.keyCode ? e.keyCode : e.which);
                    		        if (code == 13) 
                    		        	e.preventDefault();
                        });
                        
            
    }
    
    function replaceDesHTML(){
				        oldAreaText = $(this).html()
				                         .replace(/"/g, "&quot;").replace("Click to input ...", "");
				        
				        
						 $(this).removeClass("editHover");
						
				         $(this).addClass("noArea")
				        		.html("")
				               	.unbind('click', replaceTitleHTML)
				        		.unbind('click', replaceDesHTML);
				         
				         
				         var titleDiv = $(this).siblings(".photoTitle");
				         
				         oldText = titleDiv.html().replace(/"/g, "&quot;").replace("Click to input ...", "");
				         
				         titleDiv.addClass("noPad")
						         .html("")
						         .html("<form id=\"photoTitleDes_Form\" action=\"/ajax/photo/updateTitleDes\">" +
						         		"<input name=\"photoTitle\" placeholder=\"Click to input ...\" type=\"text\" class=\"span3\" value=\"" + oldText + "\" /> " +
						         		"<textarea name=\"photoDes\" placeholder=\"Click to input ...\" autofocus=\"autofocus\" class=\"span3\" id=\"textarea\" rows=\"5\">" + oldAreaText+ "</textarea>" + 
						         		"</form><div class=\"inplaceButtons\"><a href=\"#\" class=\"btn btn-primary btn-mini btnSave\">Save</a> <a href=\"#\" class=\"btn btn-primary btn-mini btnDiscard\">Cancel</a></div>")
						         .unbind('click', replaceTitleHTML)
						         .unbind('click', replaceDesHTML);
	}
    
    function showPlaceHolder(){
					    $(".photoDes").each(function(index) {
					        if($(this).html().replace("Click to input ...", "").replace(/"/g, "&quot;")==""){
					        	$(this).html("Click to input ...");
					        	$(this).css({'color':'grey'});
					        }
					        else{
					        	$(this).css({'color': ''});
					        }
					        	
					    });
					    
					    $(".photoTitle").each(function(index) {
					        if($(this).html().replace("Click to input ...", "").replace(/"/g, "&quot;")==""){
					        	$(this).html("Click to input ...");
					        	$(this).css({'color':'grey'});
					        }
					        else{
					        	$(this).css({'color': ''});
					        }
					    });
    }
}
); 

