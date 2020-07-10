$(document).ready(function(){
	
	$('#registrationSubmit').on('click', function(event) {
		event.preventDefault();
		var emailInput = $('#emailInput');
		var passwordInput = $('#passwordInput');
		var passwordInputRepeat = $('#passwordInputRepeat');
		
		var email = emailInput.val();
		var password = passwordInput.val();
		var passwordRepeat = passwordInputRepeat.val();
		
		if($('#emailInput').val() == "" || $('#passwordInput').val() == "" || $('#passwordInputRepeat').val() == ""){
            alert('Niste uneli sve potrebne informacije!');
            return;
        }
		if($('#passwordInput').val() != $('#passwordInputRepeat').val()){
            alert('Unete lozinke nisu iste!');
            return;
        }
		$.post('api/users/user/registration', {'email': email, 'password': password},
			function(response){
				alert('Uspesno ste se registrovali!');
	            window.location.replace("index.html");
		}).fail(function(){
			console.log("error")
		});
	});
});