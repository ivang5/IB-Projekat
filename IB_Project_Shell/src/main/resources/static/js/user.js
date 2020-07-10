$(document).ready(function(){

	if(sessionStorage.getItem('userEmail') == "") {
		window.location.replace('index.html');
	}
	
	
    var clicks = 0;
    $("#buttonSearch").click(function (e) {
        if(clicks == 0){
            $("#buttonSearch").html("Pretraga &#8679");
            $("#search").fadeIn( 1200 , function(){
                document.getElementById("search").style.display = "unset";
            });
        }else if (clicks % 2 !== 0) { 
            $("#buttonSearch").html("Pretraga &#8681");
            $("#search").hide();
         }else if(clicks % 2 == 0) {
            $("#buttonSearch").html("Pretraga &#8679");
            $("#search").show();
         }
         clicks++;
         e.preventDefault();
         return false;
    });
    
    
    var emailInput = $('#emailInput');
    var userTable = $('#userTable');
    var mySection = $('#mySection');
    var inputEmail = $('#inputEmail');
    var inputActive = $('#inputActive');
    var inputAuthority = $('#inputAuthority');
    var userEmail = sessionStorage.getItem('userEmail');
    
    getUsers();
    currentUserInfo(userEmail);
    
    
    function currentUserInfo(email) {
    	$.get('api/users/user/email', {'email': email},
    		function(response){
				console.log(response);
				inputEmail.val(response.email).trigger("change");
				if(response.active) {
					inputActive.val("Aktivan").trigger("change");
					if(response.certificate != "") {
						$("#buttonJks").hide();
					}
				}else {
					inputActive.val("Neaktivan").trigger("change");
					$("#buttonJks").hide();
				}
				inputAuthority.val(response.authority.name).trigger("change");
		}).fail(function(){
			console.log("error");
		});
    }
    
    
    $("#buttonJks").click(function (e) {
    	var email = sessionStorage.getItem('userEmail');
    	$.get('api/users/user/jks', {'email': email},
    		function(response){
    		$("#buttonJks").hide();
	    	alert("Uspesno napravljena JKS datoteka");
		}).fail(function(){
			console.log("error");
		});
    	e.preventDefault();
    });
    
    
    function addUser(user) {
    	var tr = $('<tr></tr>');
    	var email = $('<td class="col1 text-left">'+user.email+'</td>');
    	var uloga = $('<td class="text-left">'+user.authority.name+'</td>');
    	if(user.active) {
    		var status = $('<td class="text-left">aktivan</td>');
    	}else {
    		var status = $('<td class="text-left">neaktivan</td>');
    	}
    	
    	tr.append(email).append(uloga).append(status);
    	console.log(tr);
    	userTable.append(tr);
    }
    
    
    function getUsers() {
    	$.get('api/users/', function(data){
    		$('#userTable tr').not(function(){ return !!$(this).has('th').length; }).remove();
			console.log(data);
			for(var user of data){
				addUser(user);
			}
		}).fail(function(){
			console.log("error")
		});
    }
    
    
    function getUserByEmail (email) {
    	$.get('api/users/user/email', {'email': email},
    		function(response){
    			$('#userTable tr').not(function(){ return !!$(this).has('th').length; }).remove();
				console.log(response);
				addUser(response);
		}).fail(function(){
			console.log("error")
		});
    }
    
    
    $("#searchButton").click(function (e){
    	var email = emailInput.val();
    	if(email == "") {
    		getUsers();
    	}else {
    		getUserByEmail(email);
    	}

        event.preventDefault();
        return false;
    });
    
    
    $('#btnLogout').click(function (e){
    	sessionStorage.setItem('userEmail', "");
    });
    
});