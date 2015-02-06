// uniform
$('[data-form=uniform]').uniform();

// validate
$('#sign-in').validate();
$('#sign-up').validate();
$('#form-recover').validate();

// set cursor in the login box by default
$('#sign-in input[name=username]').focus();