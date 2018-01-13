$.date = function(dateObject) {
			    var d = new Date(dateObject);
			    var day = d.getDate();
			    var month = d.getMonth() + 1;
			    var year = d.getFullYear();
			    
			    var hours = d.getHours();
			    var min = d.getMinutes();
			    
			    if (day < 10) {
			        day = "0" + day;
			    }
			    if (month < 10) {
			        month = "0" + month;
			    }
			    var date = day + "/" + month + "/" + year + " " + hours + ":" + min;

			    return date;
			};