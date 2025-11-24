jQuery(function(){

	nav.init();
	
	$(window).on("resize", function () {
		nav.resize();
	});

	$(window).on("scroll", function () {
		nav.resize();
	});

});

//nav
var nav = {
	gnbScrollTop: 0,
	delta: 80,
	unMob: 767,

	//init
	init: function () {
		nav.checkViewport();
		nav.headerChange();

		$(document).on('click', '.toggle-nav', function () {
			if ($('body').hasClass('opened-nav')) {
				nav.closeNav();
			} else {
				nav.openNav();
			}
		});

		// PC 전용 자동 열림
        if (window.innerWidth > nav.unMob) {
          const $aside = $('#aside');
          $aside.on('mouseenter', function () {
            $('body').addClass('opened-nav');
          });
          $aside.on('mouseleave', function () {
            $('body').removeClass('opened-nav');
          });
        }

		$(window).on("scroll", function (e) {
			var st = $(this).scrollTop();

			//scroll Check
			if (st == 0) {
				$('body').removeClass('scroll-has');
			} else {
				$('body').addClass('scroll-has');

				if(st == $(document).height() - $(window).height()){
					$('body').addClass('scroll-end');
				} else {
					$('body').removeClass('scroll-end');
				}
			}

			if (Math.abs(nav.gnbScrollTop - st) <= nav.delta) return;

			//scroll up/down
			if ((st > nav.gnbScrollTop) && (nav.gnbScrollTop > 0)) {
				$('body').addClass('scroll-down').removeClass('scroll-up');
			} else {
				$('body').addClass('scroll-up').removeClass('scroll-down');
			}
			nav.gnbScrollTop = st;
		});

		// userNav
		$('.btn-toggle').each(function(){
			$(this).click(function(e){
				var myCon = $(this).siblings('.toggle-cont'),
					siblings = $(this).parent('li').siblings('li').find('.toggle-cont');

				$(myCon).addClass('active');
				$(siblings).removeClass('active');
			});
		});

		// 외부클릭시
		$(document).mouseup(function (e){
			var target = $(".toggle-cont");
			if(target.has(e.target).length==0) {
				nav.closeUserNav();
			}
		});

	},

	//checkViewport
	checkViewport: function () {
		var wWidth = window.innerWidth;

		if(wWidth > nav.unMob) {
			$('body').removeClass('is-mobile');
		} else {
			$('body').addClass('is-mobile');
		}

		//for ios vh
		let vh = window.innerHeight * 0.01;
		document.documentElement.style.setProperty('--vh', `${vh}px`);
	},

	//openNav
	openNav: function () {
		$('body').addClass("opened-nav");
	},

	//closeNav
	closeNav: function () {
		$('body').removeClass("opened-nav");
	},

	//closeUserNav
	closeUserNav: function () {
		$('.toggle-cont').removeClass('active');
	},

	//resize
	resize: function () {
		nav.checkViewport();
		nav.headerChange();
		nav.closeUserNav();
	},

	//current
	current: function (dep1, dep2) {
		var gnb = $('#nav ul > li'),
			current1 = dep1 - 1,
			gnbDep = $(gnb).eq(current1).find('li'),
			current2 = dep2 - 1;

			//console.log(dep1 + " and " + dep2);
		//dep1
		if (!dep1 == "") {
			$(gnb).eq(current1).addClass('current');
			$(gnb).eq(current1).siblings().removeClass('current');
		}

		//dep2
		if (!dep2 == "") {
			$(gnbDep).eq(current2).addClass('current');
			$(gnbDep).eq(current2).siblings().removeClass('current');
		}
	},

	//headerChange
	headerChange: function () {
		var st = $(window).scrollTop(),
			$header = $("#header"),
			headerH = $header.outerHeight();

		//header Fix
		if (st > headerH) {
			$header.addClass("fixed");
		} else {
			$header.removeClass("fixed");
		}
	}
}

// modalOpen
function modalOpen(popId){
	$(popId).addClass("active");
	$('body').addClass("modal-opened");
}

// modalClose
function modalClose(popId){
	$(popId).removeClass("active");
	$('body').removeClass("modal-opened");
}

//tabLink
function tabLink() {
	$('.tabs > li a').each(function(){
		var tabTarget = $(this).attr('href'),
			linkTarget = $(this).attr('title');

		$(this).click(function(e){
			if (linkTarget != '페이지이동'){
				e.preventDefault();
			}

			$(this).parent('li').addClass('current');
			$(this).parent('li').siblings('li').removeClass('current');
			$(tabTarget).addClass('active').siblings('.tab-content').removeClass('active');
		});
	});
}

//numberMaxLength
function numberMaxLength(e){
	if(e.value.length > e.maxLength){
		e.value = e.value.slice(0, e.maxLength);
	}
}

//fileAdd
function fileAdd() {
	//File upload
	$('.js-add-file').on('change',function(event){
		var files = event.target.files;
		//type
		if ($(this).hasClass('input-text')){
			var type = 'input',
				myCon = $(this).closest('.field').find('.file-list');
		} else {
			var type = 'box',
				myCon = $(this).closest('.upload-btn');
		}

		var maxFileCnt = 5;		// 첨부파일 최대 개수
		var attFileCnt = $(myCon).siblings('.upload-file').length;	// 기존 첨부파일 개수
		var remainFileCnt = maxFileCnt - attFileCnt;    // 추가로 첨부가능한 개수
		var curFileCnt = files.length;  // 현재 선택된 첨부파일 개수

		//console.log('max: '+attFileCnt + '/ 기존 : '+ attFileCnt + '/ 가능 : '+ remainFileCnt + '/ 현재 : '+ curFileCnt);

		for (var i = 0; i < Math.min(curFileCnt, remainFileCnt); i++) {
			const file = files[i];

			var reader = new FileReader();
			reader.onload = function(e) {

				var html = "<div class='upload-file'><span class='file-name'>" + file.name + "</span><button type='button' class='btn file-remove' onclick='fileRemove(this);' data-id='" + file.name + "' ><i class='ico delete-black op38'><span>삭제</span></i></button></div>";

				if (type == 'box'){
					if(file.type.match("image/.*")){
						var html = "<div class='upload-file img'><div class='img-wrap r4'><img src='" + e.target.result + "' class='file-preview centered'></div><button type='button' class='btn file-remove' onclick='fileRemove(this);' data-id='" + file.name + "' ><i class='ico delete-black'><span>삭제</span></i></button></div>";
					};

					if(file.type.match("video/.*")){
						var html = "<div class='upload-file img'><div class='img-wrap r4'><img src='" + e.target.result + "' class='file-preview centered'></div><button type='button' class='btn file-remove' onclick='fileRemove(this);' data-id='" + file.name + "' ><i class='ico delete-black'><span>삭제</span></i></button></div>";
					};

					$(html).insertBefore(myCon);

					var upCount = $(myCon).siblings('.upload-file').length,
						countIn = $(myCon).find('.file-count');

					$(countIn).text( upCount );

					var fileInput = $(myCon).find('input');
					if( upCount >= maxFileCnt){
						$(fileInput).attr('disabled', true);
					}

				} else {
					$(html).prependTo(myCon);
				}

			}
			reader.readAsDataURL(file);
		}
	});
}

//File remove
function fileRemove(t) {
	$([''])
	var me = $(t).parent('.upload-file'),
		myCon = $(me).closest('.upload-wrap');
	$(me).remove();

	//type
	if ($(me).hasClass('img')){
		var upCount = $(myCon).children('.upload-file').length,
			countIn = $(myCon).find('.file-count');
		$(countIn).text( upCount );

		var fileInput = $(myCon).find('input');
		var maxFileCnt = 5;		// 첨부파일 최대 개수
		if( upCount >= maxFileCnt){
			$(fileInput).attr('disabled', true);
		} else{
			$(fileInput).removeAttr('disabled');
		}
	}
}
function findAddr() {
    new daum.Postcode({
        oncomplete: function(data) {
            let addr = ''; // 주소 변수

            if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                addr = data.roadAddress;
            } else { // 사용자가 지번 주소를 선택했을 경우(J)
                addr = data.jibunAddress;
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.inputform.userAddress1.value = addr;
            // 커서를 상세주소 필드로 이동한다.
            document.inputform.userAddress2.focus();
        }
    }).open();
}
