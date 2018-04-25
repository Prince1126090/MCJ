<%@ taglib prefix="s" uri="/struts-tags"%>
<script  type="text/javascript">
	navCache("editSurcharge.action");
	setTitle("Edit Surcharge");
</script>
<link href="/JGTDSL_WEB/resources/css/page/meterReading.css" rel="stylesheet" type="text/css" />
<style>
input[type="radio"], input[type="checkbox"]
{
margin-top: -3px !important;
}
.alert{
padding-top: 4px !important;
padding-bottom: 4px !important;
}
.ui-icon, .ui-widget-content .ui-icon {
    cursor: pointer;
}
.sFont{
font-size: 12px;
}

</style>
<div class="meter-reading" style="width: 80%;height: 50%;">
	<div class="row-fluid">
		<div class="span9" id="rightSpan">
			
				<div class="w-box-header">
    				<h4 id="rightSpan_caption">Edit Surcharge</h4>
				</div>
				<div class="w-box-content" style="padding: 10px;" id="content_div">
				
     									
						<div class="row-fluid">							

							
							<div class="row-fluid">
									<div class="span9">
										<label style="width: 40%">Customer ID</label>
										<input type="text" name="customer_id" id="customer_id" style="width: 54.5%" />
									</div>
							</div>	
							<div class="row-fluid">
									<div class="span9">
										<label style="width: 40%">Year</label>
										<input type="text" name="year" id="year" style="width: 54.5%" />
									</div>
							</div>	
							<div class="row-fluid">
									<div class="span9">
										<label style="width: 40%">Month</label>
										<select name="month" id="month" style="width: 56%;margin-left: 0px;">
							       	     <option value="">Select Month</option>           
							              <s:iterator value="%{#application.MONTHS}">   
							   			<option value="<s:property value='id'/>"><s:property value="label"/></option>
									     </s:iterator>
						       </select>
							       </div>
							</div>	
							 
					   </div>
					   
					   
					   
					   
						
						
						
						
						<div class="formSep" style="padding-top: 2px;padding-bottom: 2px;">
							<div id="aDiv" style="height: 0px;"></div>
						</div>
						
						<div class="formSep sepH_b" style="padding-top: 3px;margin-bottom: 0px;padding-bottom: 2px;">		
						   <table width="100%">
						   	<tr>
						   		
						   		<td style="width: 60%" align="right">				   			     
						   			 <button class="btn" id="btn_save" onclick="fetchWrongCollectionList()">Search</button>	
									 <button class="btn btn-danger"  type="button" id="btn_cancel" onclick="callAction('blankPage.action')">Cancel</button>
						   		</td>
						   	</tr>
						   </table>								    
						   									
						</div>
					
					
				
																				
				</div>
			</div>
	</div>
	
		<div style="width: 47%;text-align: center;float: left;padding-top:20px;margin-left: 5px;display: none;" id="stat_div">
			<table>
				<tr>
					<td style="text-align: left;padding-left: 10px;padding-bottom: 20px;background-color: #387C44;color: white;"  id="loading_div"></td>
				</tr>
			</table>
		</div>


		<div id="detailDiv">
		
		</div>
	
</div>

  
<p style="clear: both;margin-top: 5px;"></p>

<script type="text/javascript">

  function fetchWrongCollectionList() {

        if($("#customer_id").val()==""){
            alert("Please enter customer id");return;
        }

		if($("#month").val()==""){
            alert("Please enter a valid month");return;
        }
		
		if($("#year").val()==""){
            alert("Please enter a valid year");return;
        }

		$("#detailDiv").html("");
		$("#stat_div").show();
		$("#loading_div").html(jsImg.LOADING_MID+"<br/><br/><font style='color:white;font-weight:bold'>Please wait. Searching the Collection list </font>");
		
		
		
        $.ajax({
            type    : "POST",
            url     : "getNMLedgerByMonthYear",
            dataType: 'text',
            async   : false,
            data    : {customer_id: $("#customer_id").val(), year: $("#year").val(), month: $("#month").val()
            }
        	}).done(function (msg) {
        			$("#stat_div").hide();
                    $("#detailDiv").html(msg);
                })
                .always(function () {
                
                })
                .fail(function (data) {
                    if (data.responseCode)
                        alert(data.responseCode);
                });

		
    }   


</script>	
