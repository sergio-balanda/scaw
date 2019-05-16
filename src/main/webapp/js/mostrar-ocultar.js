function show(clave) {
	document.getElementById("myText").value = clave;
	//this.Toggle();
}
function Toggle() {
	//var inputType = $('#myText').attr('type');
	if (document.getElementById("myText").type == "hidden") {
		document.getElementById("myText").type = "text";
	} else {
		document.getElementById("myText").type = "hidden";
	}
}
