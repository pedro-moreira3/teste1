var alerts = [];
var alertsTimers = [];
var closeButtons = [];
			
function guidGenerator() {
	var S4 = function() {
		return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
	};
	return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
}
			
function getUniqueId() {
	while(true) {
		var id = guidGenerator();
		if(alerts.indexOf(id) < 0)
			return id;
	}
}
		
function addAlert(titulo, mensagem, tipo, tempo, iconName) {
	var alert = document.createElement('div');
	alert.classList.add('alert-box');
	alert.classList.add('animate-opacity');
	alert.classList.add('alert-type-' + tipo);
	alert.id = getUniqueId();
	var icon = document.createElement('div');
	icon.classList.add('alert-icon');
	var iconImg = document.createElement('i');
	iconImg.classList.add('topbar-icon');
	iconImg.classList.add('material-icons');
	iconImg.innerText = iconName;
	var content = document.createElement('div');
	content.classList.add('alert-content');
	var title = document.createElement('div');
	title.classList.add('alert-title');
	title.innerText = titulo;
	var message = document.createElement('div');
	message.classList.add('alert-message');
	message.innerText = mensagem;
	var close = document.createElement('div');
	close.classList.add('alert-close');
	var closeImg = document.createElement('img');
	closeImg.classList.add('alert-close-img');
	closeImg.src = '/intelidente/javax.faces.resource/close-icon.png.jsf?ln=images';
	content.appendChild(title);
	content.appendChild(message);
	icon.appendChild(iconImg);
	alert.appendChild(icon);
	alert.appendChild(content);
	close.appendChild(closeImg);
	alert.appendChild(close);
	
	Array.from(document.getElementsByClassName("alert-container")).forEach(
	    function(element, index, array) {
			element.appendChild(alert);
		}
	);
	
	alerts[alert.id] = alert;
	alertsTimers[alert.id] = setTimeout(function() {
		$(alert).fadeOut(300, function() {
			closeAlert(alert);
		});
	}, tempo);
		
	closeButtons = $('.alert-close-img');
	closeButtons.on('click', function() {
		closeAlert($(this).parent().parent()[0]);
	});
}
			
function closeAlert(alert) {
	Array.from(document.getElementsByClassName("alert-container")).forEach(
	    function(container, containerIndex, containerArray) {
			var nodeRemove = null;
			Array.from(container.childNodes).forEach(
				function(alertBox, alertBoxIndex, alertBoxArray) {
					if(alertBox.id == alert.id) {
						nodeRemove = alertBox;
					}
				}
			);
			if(nodeRemove != null)
				container.removeChild(nodeRemove);
		}
	);
	alerts.pop(alert.id);
	alertsTimers.pop(alert.id);
}