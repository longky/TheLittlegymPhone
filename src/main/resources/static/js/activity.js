/**
 * Created by TONY on 2017/10/10.
 */

/****************动态加载开始***********************************/
$("#home").addClass("active");
var loading = false;
// 最多可加载的条目
var maxItems = 40;
// 每次加载添加多少条目
var itemsPerLoad = 4;
//初始值
var lastIndex=1;



// 接上次加载的序号，并注册
$(document).on('infinite', '.infinite-scroll-bottom', function () {
    if (loading) return;
    loading = true;
    // 模拟1s的加载过程
    setTimeout(function () {
        // 重置加载flag
        loading = false;

        // 添加新条目
        lastIndex++;
        if (lastIndex*itemsPerLoad >= maxItems) {
            // 加载完毕，则注销无限加载事件，以防不必要的加载
            $.detachInfiniteScroll($('.infinite-scroll'));
            // 删除加载提示符
            $('.infinite-scroll-preloader').remove();
            return;
        }

        ajax_getItems(itemsPerLoad,lastIndex,$('#keyword').val());
        // 更新最后加载的页面
        //lastIndex = $('.card-container .card').length;
        //容器发生改变,如果是js滚动，需要刷新滚动
        $.refreshScroller();
    }, 1000);

});

$(document).on('refresh', '.pull-to-refresh-content', function (e) {
    // 模拟2s的加载过程
    setTimeout(function () {
        // 加载完毕需要重置
        $.pullToRefreshDone('.pull-to-refresh-content');
        location.reload();
    }, 2000);

});



function toggleLike() {
    var iconLike = $("#icon-like i");
    if (iconLike.hasClass("fa fa-heart fa-2x")) {
        $("#icon-like i").removeClass("fa fa-heart fa-2x");
        $("#icon-like i").addClass("fa fa-heart-o fa-2x");
        $.toast("已取消", 500);
    } else {
        $("#icon-like i").removeClass("fa fa-heart-o fa-2x");
        $("#icon-like i").addClass("fa fa-heart fa-2x");
        $.toast("已喜欢", 500);
    }
}

$("#add_val").on('click',function () {
    var tel = $("#add_phone").val();
    if (!checkTel(tel)){
        $.alert("请输入正确的手机号");
        return;
    }
    ajax_val(tel);
})

var waitTime = 60;
var valTimer;
function time(o) {
    if (waitTime == 0) {
        o.removeAttr("disabled");
        o.removeClass("disabled");
        o.text("发送验证码");
        waitTime = 60;
    } else {
        o.addClass("disabled");
        o.attr("disabled",true);
        o.text(waitTime + "s重新获取");
        waitTime--;
        valTimer = setTimeout(function () {
                time(o);//循环调用
            },
            1000)
    }
}

function checkAdd() {
    if (!checkTel($("#add_phone").val())) {
        $.alert("请输入正确的手机");
        return false;
    }
    if ($.trim($("#add_name").val()).length > 16 || $.trim($("#add_name").val()) == '') {
        $.alert("请输入正确的名字");
        return false;
    }
    if ($.trim($("#role").val())=="家长/孩子"||$.trim($("#role").val())=="") {
        $.alert("请选择家长/孩子");
        return false;
    }
//        if (!/^\d{4}$/.test(($.trim($("#add_num").val())))){
//            $.alert("验证码为4位数字");
//            return false;
//        }
    return true;
}



/****************myinfo-开始***********************************/
$("#info").on('click', function () {
    $.popup('.popup-profile');
});



$("#city-picker").cityPicker({});
$("#changeCity").on('click', function () {
    $("#city-picker").picker("open");
})



/*activity_Enrol*/
$("div[id^='icon-']").on('click', function () {
    idNative = $(this).attr("id");
    idOffset = idNative.split("-")[1];
    if (idOffset != 'review' && idOffset != 'like') {
        $('.content').scrollTop($("#" + idOffset).offset().top)
    }
});

$("#icon-like").on('click', function () {
    toggleLike();
});



//默认diable
$("#pickerActivity").toggleClass("disabled");
$("#agree").on('change', function () {
    var is_checked = $(this).prop("checked");
    if(is_checked){
        if($("#pickerActivity").hasClass("disabled")){
             $("#pickerActivity").removeClass("disabled");
        }
    }else{
        if(!$("#pickerActivity").hasClass("disabled")) {
            $("#pickerActivity").toggleClass("disabled");
        }
    }
})







$("#update_submit").on('click',function () {
    if (checkUpdatePar()){
        var $up_name = $('.up-name');
        var up_name = '';
        for(name in $up_name){
            up_name = name.val() +'、';
        }
        //TODO
        var actId = 1;
        ajax_update_par(id,actId,up_name);
    }
    ajax_update_par();
    $.alert("修改成功");
})

function resetPopup(popup){
    var $popup = $(popup);
    $popup.find('input').val('');//清空表单
    var val = $popup.find('button');
    val.removeAttr('disabled');
    val.removeClass('disabled');
    val.text('获取验证码');
    clearTimeout(valTimer);//移除定时器
}


$("#role").picker({
    toolbarTemplate: '<header class="bar bar-nav">\
  <button class="button button-link pull-left">按钮</button>\
  <button class="button button-link pull-right close-picker">确定</button>\
  <h1 class="title">家长/孩子</h1>\
  </header>',
    cols: [
        {
            textAlign: 'center',
            values: ['家长', '孩子']
        }
    ]
});

function setId(id){
    $("#activity").data("value",id);
}

function ajax_getItems(size, index, kw) {
    var html = '';
    $.ajax({
        type: 'GET',
        url: '/activity/getItems',
        data: {
            'size': size,
            'pageNow': index,
            'kw': kw
        },
        async: false,
        contentType: "application/x-www-form-urlencoded",
        dataType: "json",
        success: function (res) {
            if (!res.code) {
                if (res.data.content.length > 0) {
                    $.each(res.data.content, function (index, activity) {

                        html = '<a class="card animated pulse" href="#activity" onclick="setId(' + activity.id + ')"  data-value="' + activity.id + '" >' +
                            '<div class="card-header no-border no-padding">' +
                            '<img class="card-cover" src=" ' + (activity.bannerSrc ? activity.bannerSrc : '/images/member/inform.jpg') + '"/>' +
                            '</div>' +
                            '<div class="card-content">' +
                            '<div class="card-content-inner">' +
                            '<div class="gym-activity-title">' + activity.name +
                            '</div>' +
                            '<div class="gym-activity-item">' +
                            '<div class="avitivity-text">' +
                            '<p>日期: ' + toDate(activity.beginDate) + '~' + toDate(activity.endDate) + '</p>' +
                            '<p>运动: ' + activity.type + '</p>' +
                            '</div>' +
                            '<i class="fa fa-bicycle fa-2x color-primary" aria-hidden="true"></i>' +
                            '</div>' +
                            '</div>' +
                            '</div>' +
                            '</a>';
                        $('.infinite-scroll-bottom .card-container').append(html);
                    })
                    $.refreshScroller();
                    if(res.data.last){
                        $('.infinite-scroll-preloader').remove();
                    }
                } else {
                    $.detachInfiniteScroll($('.infinite-scroll'));
                    //$('.infinite-scroll-preloader').text("没有了~");
                    $('.infinite-scroll-preloader').remove();
                    $.toast("没有记录了~")
                    return;
                }

            }
        }
    })

}


/*****************activity-开始*******************************/
function ajax_activity(id) {

    //loading 活动
    $.ajax({
        type: 'POST',
        url: '/activity/view',
        data: {
            'id': id
        },
        async: false,
        contentType: "application/x-www-form-urlencoded",
        dataType: "json",
        success: function (activity) {
            //alert(JSON.stringify(activity))
            if(activity){
                $("#activity_name").text(activity.name);
                $("#activity_type").text(activity.type);
                $("#activity_chargeType").text(activity.chargeType);
                $("#activity_crowd").text(activity.crowd);
                $("#activity_strength").text(activity.strength);
                $("#activity_beginDate").text(toDate(activity.beginDate));
                $("#activity_detail").text(activity.detail);
                if (activity.bannerSrc) {
                    $("#activity_banner").css("backgroundImage", "url(" + activity.bannerSrc + ")");
                } else {
                    $("#activity_banner").css("backgroundImage", "url('/images/member/classThemeBanner.jpg')");
                }
            }
        }
    });
}
/*****************activity-结束*******************************/


/*****************search-开始**********************************/
function submitSearch() {
    $('.card-container .card').remove();
    $(".mycity").text($('#keyword').val());
    ajax_getItems(itemsPerLoad, 1, $('#keyword').val());
    $('#keyword').blur()
    $(".content").scrollTop(0);
    $.router.load("#home");
    return false;
}
/*****************search-结束**********************************/


function ajax_val(tel) {
    $.ajax({
        type: "POST",
        url: "/activity/val",
        data: {"tel": tel},
        contentType: "application/x-www-form-urlencoded",
        dataType: "json",
        success: function (data) {
            if (data.success == true) {
                console.info(data.message);
                time($("#add_val"));
            } else {
                $.alert("发送失败，稍后再试");
            }
        }
    });
}



function ajax_add(tel, name,actid,num) {
    $.ajax({
        type: "POST",
        url: "/activity/add",
        data: {"tel": tel,"name":name,"actId":actid,"num":num},
        contentType: "application/x-www-form-urlencoded",
        dataType: "json",
        success: function (data) {
            if ( data != null && data.success == true) {
                $.alert(data.message);
            }else{
                $.alert(data.message);
                var html = '<lable class="gym-person-name">、' + data.message + '</label>';
                $('.gym-main').append(html);
            }
            $.closeModal(".popup-add");
            resetPopup(".popup-add");
        }
    });
}

function ajax_update_par(id, actid,extraname) {
    $.ajax({
        type: "POST",
        url: "/activity/update_par",
        data: {"id": id,"actId":actid,"extraname":extraname},
        contentType: "application/x-www-form-urlencoded",
        dataType: "json",
        success: function (data) {
            $.alert('修改成功');
        }
    });
}




