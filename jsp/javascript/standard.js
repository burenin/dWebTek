// Form functions

function initFormFields(form, field) {
	var wiki = unescape(getParameter("wiki")).replace(/\+/g, " ");
	var checkAll = false;
	if (wiki == "") {
		checkAll = true;
	}
	for (var i = 0; i < field.length; i++) {
		if (wiki.indexOf(field[i].value) != -1 || checkAll) {
			field[i].checked = true;
		}
	}
	var pattern = unescape(getParameter("pattern")).replace(/\+/g, " ");
	if (pattern != "") {
		form.pattern.value = pattern;
	}
	var luck = unescape(getParameter("luck")).replace(/\+/g, " ");
	if (luck == "on") {
		form.luck.checked = true;
	}
}

function setAction(form) {
	if (form.pattern.value == "Search...") {
		form.pattern.value = "";
	}
	if (document.servers.server) {
		var field = document.servers.server;
		var fields = new Array();
		var allChecked = true;
		for (var i = 0; i < field.length; i++) {
			if (field[i].checked) {
				fields.push(field[i].value);
			} else {
				allChecked = false;
			}
		}
		var wiki = fields.join(",");
		if (wiki != "" && allChecked == false) {
			var input = document.createElement("input");
			input.setAttribute("type", "hidden");
			input.setAttribute("name", "wiki");
			input.setAttribute("value", wiki);
			form.appendChild(input);
		}
	}
}

function checkAll(field) {
	for (var i = 0; i < field.length; i++) {
		field[i].checked = true;
	}
}
function uncheckAll(field) {
	for (var i = 0; i < field.length; i++) {
		field[i].checked = false;
	}
}

// Other

function getParameter(name) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if(results == null) {
		return "";
	} else {
		return results[1];
	}
}