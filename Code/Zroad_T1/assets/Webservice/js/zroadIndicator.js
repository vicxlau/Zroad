function rotateZroadIndicator(rotate_angle){
	//document.getElementById("zroadIndicator").src = new AR.ImageResource("assets/indi.png"),
	//$("#zroadIndicator").attr("src","assets/indi.png");
	//$("#zroadIndicator").rotate("30deg");
	$("#zroadIndicator").rotate({
            duration: 210,
            animateTo:rotate_angle
      });
}
            //angle: 180,
            //animateTo:90

function changeStatus(str){
	//document.getElementById("statusElement").innerHTML = str;
	$("#statusElement").html(str);
}
