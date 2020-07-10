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
    
    currentUserInfo(userEmail);
    
    function currentUserInfo(email) {
    	$.get('api/users/user/email', {'email': email},
    		function(response){
				console.log(response);
				inputEmail.val(response.email).trigger("change");
				if(response.active) {
					inputActive.val("Aktivan").trigger("change");
				}else {
					inputActive.val("Neaktivan").trigger("change");
				}
				inputAuthority.val(response.authority.name).trigger("change");
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