(function(i,s,o,g,r,a,m) {
	i['GoogleAnalyticsObject']=r;
	i[r]=i[r] || function() {
		(i[r].q=i[r].q || []).push(arguments)
	},
	i[r].l=1*new Date();
	a=s.createElement(o),
	m=s.getElementsByTagName(o)[0];
	a.async=1;
	a.src=g;
	m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-39765565-1', 'auto');
ga('require', 'displayfeatures');
ga('require', 'linkid', 'linkid.js');
ga('send', 'pageview');

/* These are all the events for the page */
$('#slider1-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'slider1-trial');
});

$('#slider2-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'slider2-trial');
});

$('#slider3-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'slider3-trial');
});

$('#hero-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'hero-trial');
});

$('#basic-plan-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'basic-plan-trial');
});

$('#standard-plan-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'standard-plan-trial');
});

$('#pro-plan-trial-link').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'pro-plan-trial');
});

$('#login-top').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'login-top');
});

$('#signup-top').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'signup-top');
});

$('#pricing-top').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'pricing-top');
});

$('#demo-top').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'demo-top');
});

$('#login-bottom').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'login-bottom');
});

$('#signup-bottom').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'signup-bottom');
});

$('#pricing-bottom').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'pricing-bottom');
});

$('#demo-bottom').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'demo-bottom');
});

$('#login-float').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'login-float');
});

$('#signup-float').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'signup-float');
});

$('#pricing-float').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'pricing-float');
});

$('#demo-float').on('click', function() {
	  ga('send', 'event', 'link', 'click', 'demo-float');
});

$('#create-account-button').on('click', function() {
	  ga('send', 'event', 'button', 'click', 'create-account');
});

$('#login-signup-link').on('click', function() {
	  ga('send', 'event', 'button', 'click', 'login-signup');
});



