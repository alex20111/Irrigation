<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="hasActionErrors()">	
	<div class="alert alert-danger"> 
		<div class="fa fa-times-circle"></div>
		<div>
			<ul class="list-unstyled">
				<s:iterator value="actionErrors">
					<li><s:property escape="false" /></li>		
				</s:iterator>
			</ul>
		</div>
	</div>	
</s:if>

<s:if test="hasFieldErrors()">	
	<div class="alert alert-danger"> 
		<div class="fa fa-exclamation-triangle"></div>
		<div>
			
		<ul>
			<s:iterator value="fieldErrors">
				<s:iterator value="value" >
					<li>
						<s:property />
					</li>
				</s:iterator>				
			</s:iterator>
		</ul> 
		</div>
	</div>	
</s:if>

<s:if test="hasActionMessages()">	
	<div class="alert alert-success"> 
		<div class="fa fa-info-circle"></div>
		<div>
		<ul class="list-unstyled">
			<s:iterator value="actionMessages">
				<li><s:property escape="false" /></li>		
			</s:iterator>
		</ul>
		</div>
	</div>	
</s:if>


