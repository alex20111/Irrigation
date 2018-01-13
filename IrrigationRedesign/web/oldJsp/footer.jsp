 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
 
 <div id="scroll">
      <a title="Scroll to the top" class="top" href="#"><img src="images/top.png" alt="top" /></a>
    </div>
    <footer>
      <p><s:a href="%{MainPageUrl}" title="Home" onclick="document.body.style.cursor='wait';">Home</s:a>  </p>
      <p>Copyright Alexandre Boudreault | <a href="http://www.css3templates.co.uk">design from css3templates.co.uk</a></p>
    </footer>
  </div>
  <!-- javascript at the bottom for fast page loading -->
  
  <script type="text/javascript" src="js/jquery.easing-sooper.js"></script>
  <script type="text/javascript" src="js/jquery.sooperfish.js"></script>
  <script type="text/javascript">
    $(document).ready(function() {
      $('ul.sf-menu').sooperfish();
      $('.top').click(function() {$('html, body').animate({scrollTop:0}, 'fast'); return false;});
      
      <s:if test="#session.user != null">
    	var interval = 10000; // 

    	setInterval(ajax_call, interval);
      </s:if>
      
   
    });
    
    var ajax_call = function() {
  	  console.log("calling");
  	};
    
    
  </script>
</body>
</html>