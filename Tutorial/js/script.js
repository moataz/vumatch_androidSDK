$(document).ready(function(){
	var selector = '.nav-stacked li';

	$(selector).on('click', function(){
	    $(selector).removeClass('active');
	    $(this).addClass('active');
	});
});

$(window).scroll(function () {
               $('.nav-stacked.affix').width($('.col-md-3').width());
           });
