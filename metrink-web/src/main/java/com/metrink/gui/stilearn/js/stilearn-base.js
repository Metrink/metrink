$(function(){
    // control for responsive
    $(window).resize(function(){
        if(sessionStorage.mode == 4){
            // control for responsive
            if($(window).width() > 767){
                data_scroll = 40 - parseInt($(this).scrollTop());
                $('.side-left, .side-right').css({
                    'top' : data_scroll+'px'
                });
                $('body, html').animate({
                    scrollTop : 0
                })
            }
            else{
                $('.side-left, .side-right').css({
                    'top' : '0px'
                });
            }
        }
        else{
            if($(window).width() <= 767){
                $('.side-left, .side-right').css({
                    'top' : '0px'
                });
            }
            else{
                $('.side-left, .side-right').css({
                    'top' : '40px'
                });
            }
        }
    });
    
    
    // scrolling event
    $(window).scroll(function() {
        
        // this for hide/show button to-top
        if($(this).scrollTop() > 480) {
            $('a[rel=to-top]').fadeIn('slow');	
        } else {
            $('a[rel=to-top]').fadeOut('slow');
        }
        
        // this for sincronize active sidebar item
        if($(this).scrollTop() > 35){
            if(sessionStorage.mode == 3 || sessionStorage.mode == 4 ){
                $('.sidebar > li:first-child.active').removeClass('first');
            }
        }
        else{
            $('.sidebar > li:first-child.active').addClass('first');
        }
        
        if(sessionStorage.mode){
            if(sessionStorage.mode == 4){
                if($(this).scrollTop() > 40){
                    $('.side-left, .side-right').css({
                        'top' : '0px'
                    });
                }
                else{
                    // control for responsive
                    if($(window).width() > 767){
                        data_scroll = 40 - parseInt($(this).scrollTop());
                        $('.side-left, .side-right').css({
                            'top' : data_scroll+'px'
                        });
                    }
                    else{
                        $('.side-left, .side-right').css({
                            'top' : '0px'
                        });
                    }
                }
            }
            else{
                $('.header').css({
                    'top' : '0px'
                });
            }
        }
        
    });
    
    $('a[rel=to-top]').click(function(e) {
        e.preventDefault();
        $('body,html').animate({
            scrollTop:0
        }, 'slow');
    });
    // end scroll to top
    
    
    // tooltip helper
    $('[rel=tooltip]').tooltip();	
    $('[rel=tooltip-bottom]').tooltip({
        placement : 'bottom'
    });	
    $('[rel=tooltip-right]').tooltip({
        placement : 'right'
    });
    $('[rel=tooltip-left]').tooltip({
        placement : 'left'
    });	
    // end tooltip helper
    
    
    // animate scroll, define class scroll will be activate this
    $(".scroll").click(function(e){
        e.preventDefault();
        $("html,body").animate({scrollTop: $(this.hash).offset().top-40}, 'slow');
    });
    // end animate scroll
    
    
    // control box
    // collapse a box
    $('.header-control [data-box=collapse]').click(function(){
        var collapse = $(this),
        box = collapse.parent().parent().parent();

        collapse.find('i').toggleClass('icofont-caret-up icofont-caret-down'); // change icon
        box.find('.box-body').slideToggle(); // toggle body box
    });
    
    // collapse on load
    $('.box-body[data-collapse=true]').slideUp() // slide up onload
    .parent() // on .box
    .find('.header-control [data-box=collapse] i').toggleClass('icofont-caret-up icofont-caret-down'); // find the controller and change default icon
    
    // close a box
    $('.header-control [data-box=close]').click(function(){
        var close = $(this),
        box = close.parent().parent().parent(),
        data_anim = close.attr('data-hide'),
        animate = (data_anim == undefined || data_anim == '') ? 'fadeOut' : data_anim;

        box.addClass('animated '+animate);
        setTimeout(function(){
            box.hide()
        },1000);
    });
    // end control box
    
    // toggle sideright
    toggle_sideright = false;
    $('.side-right[data-toggle=on]').animate({
        right: "-="+216+"px"
    });
    $('.side-right.side-right-large[data-toggle=on]').animate({
        right: "-="+329+"px" // total = 545px (216+329)
    });
    $('.sideright-toggle-nav').click(function(){
        //$('.content').toggleClass('.content-large').parent().toggleClass('span11 span9');
        width_sideright_cur = $('.side-right[data-toggle=on]').width();
        $('.sideright-toggle-nav > i').toggleClass('icofont-arrow-left icofont-arrow-right');
        
        if(toggle_sideright == false){
            $('.side-right[data-toggle=on], .sideright-toggle-nav').animate({
                right: "+="+width_sideright_cur+"px"
            });
            toggle_sideright = true;
        }
        else{
            $('.side-right[data-toggle=on], .sideright-toggle-nav').animate({
                right: "-="+width_sideright_cur+"px"
            });
        
            toggle_sideright = false;
        }
        
        return false;
    });
    
    $(window).scroll(function() {
        if($(this).scrollTop() > 40){
            $('.side-right[data-toggle=on]').css({
                'top' : '0px'
            });
        }
        else{
            $('.side-right[data-toggle=on]').css({
                'top' : '40px'
            });
        }
    });
    // end toggle sideright
    
    // helper ie9
    var browser = $.browser;
    if ( browser.msie && browser.version == "9.0" ) {
        $('.input-icon-append .grd-white').css({
            'filter' : "none"
        })
    }
    
    // Resizing the content-body to take 100% of the window. NOTE: I'm not sure what's causing the off by one.
    var content_body = $(".content > .content-body");
    var window_height = $(window).height();
    var content_padding = content_body.outerHeight() - content_body.height();
    content_body.css("min-height", window_height - content_body.position().top - content_padding - 1);
})