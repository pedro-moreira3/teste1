function confirmar(remoteCommand) {
	confirmDialog("Deseja remover este registro?", "Você não poderá recuperar este registro!", "warning");
}

function confirmDialog(titulo, texto, tipo) {
	swal({
		title : titulo,
		text : texto,
		icon : tipo,
		buttons: ["Não, cancelar!", "Sim, tenho certeza!"],
		dangerMode: true,
	}).then((willDelete) => {
		if (willDelete) {
			actionRemover();
		}
	});
}

function message(title, text, type) {
	message(title, text, type, false);
}

function mostraDialogo(titulo, mensagem, tipo, tempo) {

	if ($("#container").is(":visible")) {
		return;
	}

	var icon = "";
	var color = "";

	if (!tempo) {
		var tempo = 3000;
	}

	if (!tipo) {
		var tipo = "info";
	} else if (tipo == 'success') {
		icon = 'check_circle';
		color = '#32CD32';
	} else if (tipo == 'error') {
		icon = 'error';
		color = '#FF0000';
	} else if (tipo == 'warning') {
		icon = 'warning';
		color = '#FFFF00';
	} else if (tipo == 'info') {
		icon = 'info';
		color = '#0000FF';
	}

	var container = 'margin-top:10px !important;display: block !important;width: 400px !important;height: 60px !important;padding-left: 20px !important;background-color: '
			+ color
			+ ' !important;color: black !important;opacity: 1 !important;position:fixed !important;right:0;transition: opacity 0.6s !important;top:0 !important;z-index:9999 !important;float:right !important;border-radius:8px;border:1px solid '
			+ color + ';';
	var icone = 'position:relative; display: inline-block !important; width: 60px !important; height: 100% !important; text-align:center; background-color: white;';
	var subContainer = 'display: inline-block !important; width: calc(100% - 60px) !important; height: calc(100% - 6px) !important; background-color: white !important; float: right !important; padding-top: 3px; padding-bottom: 3px;';
	var title = 'display: block !important;height: 40% !important;background-color: white !important;text-align:left;margin-top:10px;';
	var message = 'display: block !important;height: 40% !important;background-color: white !important;text-align:left;margin-top:15px;margin-bottom:15px;';

	var styleTitle = 'position: relative; top: 0; left: 0; width: 100%; height: 20px; font-weight: bold; display: table-cell; vertical-align: middle;';
	var styleMessage = 'position: relative; top: 0; left: 0; width: 100%; height: 40px;';

	var dialogo = "";

	dialogo += '<div id="container" style="'+container+'">';
	dialogo += '	<div id="icone" style="'+icone+'">';
	dialogo += '		<i class="topbar-icon material-icons" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);">'
			+ icon + '</i>';
	dialogo += '	</div>';
	dialogo += '	<div id="subContainer" style="'+subContainer+'">';
	dialogo += '		<div style="' + styleTitle + '">';
	dialogo += titulo;
	dialogo += '		</div>';
	dialogo += '		<div style="' + styleMessage + '">';
	dialogo += mensagem;
	dialogo += '		</div>';
	dialogo += '	</div>';
	dialogo += '</div>';

	// adiciona ao body a mensagem com o efeito de fade
	$("body").append(dialogo);
	$("#container").hide();
	$("#container").fadeIn(200);

	// contador de tempo para a mensagem sumir
	setTimeout(function() {
		$('#container').fadeOut(300, function() {
			$(this).remove();
		});
	}, tempo); // milliseconds

}

function message(title, text, type, fechaSozinho) {
	// swal(title, text, type);
	if(fechaSozinho) {
		swal({
	        title   : title,
	        text    : text,
	        icon	: type,
	        timer 	: 1500
	    });
	} else {
		swal({
	        title   : title,
	        text    : text,
	        icon	: type
	    });
	}
}

function fechaMenu() {
	if($('body').hasClass('SlimMenu') == false){
		$('#menu-resize-btn').trigger( "click" );
	}
}

function validaNumeros(evt) {
	var theEvent = evt || window.event;
	var key = theEvent.keyCode || theEvent.which;
	console.debug(key);
	 if( key == 8)
	        return true;
	key = String.fromCharCode(key);
	var regex = /[0-9]/;
	if (!regex.test(key)) {
		theEvent.returnValue = false;
		if (theEvent.preventDefault)
			theEvent.preventDefault();
	}
} 

function topPage() {
				$('html, body').animate({
					scrollTop : 0
				}, 'slow');
			}

		PrimeFaces.locales['pt'] = {
			closeText : 'Fechar',
			prevText : '&#x3c;Anterior',
			nextText : 'Próximo&#x3e;',
			currentText : 'Hoje',
			monthNames : [ 'Janeiro', 'Fevereiro', 'Março', 'Abril',
					'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro',
					'Novembro', 'Dezembro' ],
			monthNamesShort : [ 'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun',
					'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez' ],
			dayNames : [ 'Domingo', 'Segunda-feira', 'Terça-feira',
					'Quarta-feira', 'Quinta-feira', 'Sexta-feira',
					'Sábado' ],
			dayNamesShort : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
					'Sábado' ],
			dayNamesMin : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
					'Sábado' ],
			weekHeader : 'Sm',
			firstDay : 1,
			isRTL : false,
			showMonthAfterYear : false,
			yearSuffix : '',
			month : 'Mês',
			week : 'Semana',
			day : 'Dia',
			allDayText : 'Todo Dia'
		};

		jQuery(function($) {
			if ($.datepicker) {
				$.datepicker.regional['pt-BR'] = {
					closeText : 'Fechar',
					prevText : '&#x3c;Anterior',
					nextText : 'Pr&oacute;ximo&#x3e;',
					currentText : 'Hoje',
					monthNames : [ 'Janeiro', 'Fevereiro', 'Mar&ccedil;o',
							'Abril', 'Maio', 'Junho', 'Julho', 'Agosto',
							'Setembro', 'Outubro', 'Novembro', 'Dezembro' ],
					monthNamesShort : [ 'Jan', 'Fev', 'Mar', 'Abr', 'Mai',
							'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez' ],
					dayNames : [ 'Domingo', 'Segunda-feira',
							'Terça-feira', 'Quarta-feira',
							'Quinta-feira', 'Sexta-feira', 'Sábadoado' ],
					dayNamesShort : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
							'Sábado' ],
					dayNamesMin : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
							'Sábado' ],
					weekHeader : 'Sm',
					dateFormat : 'dd/MM/yyyy hh:mm:ss',
					firstDay : 0,
					isRTL : false,
					showMonthAfterYear : false,
					yearSuffix : ''
				};
				$.datepicker.setDefaults($.datepicker.regional['pt-BR']);
			}
			if ($.timepicker) {
				$.timepicker.regional['pt-BR'] = {
					currentText : "Agora",
					closeText : "OK",
					ampm : false,
					timeFormat : "hh:mm:ss",
					timeOnlyTitle : "Definição do Horário",
					timeText : "Horário",
					hourText : "Hora",
					minuteText : "Minuto",
					secondText : "Segundo",
					showSecond : false
				};
				$.timepicker.setDefaults($.timepicker.regional['pt-BR']);
			}

			// masktel();

		});

		function masktel(objeto) {
			$(objeto)
					.mask(
							'(00) 0000-0000',
							{
								onKeyPress : function(phone, e, currentField,
										options) {
									var new_sp_phone = phone
											.match(/^(\([129][123456789]\) 9(5[0-9]|6[0-9]|7[01234569]|8[0-9]|9[0-9])[0-9]{1})/g);
									new_sp_phone ? $(currentField).mask(
											'(00) 00000-0000', options) : $(
											currentField).mask(
											'(00) 0000-0000', options)
									// 11 950
									// 11 96
									// 11 97
									// 11 98
									// 11 99

								}
							});
		}

		function handleAjaxRequest(xhr, status, args) {
			var xmlDoc = xhr.responseXML;
			errorNodes = xmlDoc.getElementsByTagName('error-name');
			if (errorNodes.length == 0)
				return;
			errorName = errorNodes[0].childNodes[0].nodeValue;
			switch (errorName) {
			case 'class javax.faces.application.ViewExpiredException':
				alert('Sessão expirada, redirecionando para tela inicial!');
				window.location.href = 'login.jsf';
				break;
			}
		}

		function unmaskDocumento(objeto){
			$(objeto).mask("00000000000000",null);
		}
		function maskDocumentoTabView(objeto) {
			documento = document.getElementById('lume:tabView:documento');
			if(documento.value.length > 11)
				$(objeto).mask("00.000.000/0000-00",null);
			else
				$(objeto).mask("000.000.000-00",null);
		}

		function maskDocumento(objeto) {
			documento = document.getElementById('lume:documento');
			if(documento.value.length > 11)
				$(objeto).mask("00.000.000/0000-00",null);
			else
				$(objeto).mask("000.000.000-00",null);
		}

		function updateServer(text) {
			document.getElementById('lume:tabView:text').value = text;
			document.getElementById('lume:tabView:salvaBiometria').click();
		}
		function updateServerPaciente(text) {
			document.getElementById('lume:tabView:textPaciente').value = text;
			document.getElementById('lume:tabView:salvaBiometriaPaciente')
					.click();
		}
		
		function mascaraTelefone(){
			jQuery("input.telefone").mask("(99) 9999-9999?9").focusout(
					function(event) {
						var target, phone, element;
						target = (event.currentTarget) ? event.currentTarget
								: event.srcElement;
						phone = target.value.replace(/\D/g, '');
						element = $(target);
						element.unmask();
						if (phone.length > 10) {
							element.mask("(99) 99999-999?9");
						} else {
							element.mask("(99) 9999-9999?9");
						}
					});
		}
				
		$( document ).ready(function() {	
			jQuery("input.telefone").mask("(99) 9999-9999?9").focusout(
				function(event) {
					var target, phone, element;
					target = (event.currentTarget) ? event.currentTarget
							: event.srcElement;
					phone = target.value.replace(/\D/g, '');
					element = $(target);
					element.unmask();
					if (phone.length > 10) {
						element.mask("(99) 99999-999?9");
					} else {
						element.mask("(99) 9999-9999?9");
					}
				});
		});
		
		
		/* <![CDATA[ */
		function mascaraMutuario(o,f){
		 v_obj=o;
		    v_fun=f;
		    setTimeout('execmascara()',1);
		}
		function execmascara(){
		    v_obj.value=v_fun(v_obj.value);
		}
		function documento(v){				
			 // Remove tudo o que não é dígito
		    v=v.replace(/\D/g,"");
		    var n = v.length;
		    if (n <= 11) {
		    	// Coloca um ponto entre o terceiro e o quarto dígitos
		        v=v.replace(/(\d{3})(\d)/,"$1.$2");
		        // Coloca um ponto entre o terceiro e o quarto dígitos
		        // de novo (para o segundo bloco de números)
		        v=v.replace(/(\d{3})(\d)/,"$1.$2");
		        // Coloca um hífen entre o terceiro e o quarto dígitos
		        v=v.replace(/(\d{3})(\d{1,2})$/,"$1-$2");
		    } else { // CNPJ
		        // Coloca ponto entre o segundo e o terceiro dígitos
		        v=v.replace(/^(\d{2})(\d)/,"$1.$2");
		        // Coloca ponto entre o quinto e o sexto dígitos
		        v=v.replace(/^(\d{2})\.(\d{3})(\d)/,"$1.$2.$3");				 
		        // Coloca uma barra entre o oitavo e o nono dígitos
		        v=v.replace(/\.(\d{3})(\d)/,".$1/$2");
		        // Coloca um hífen depois do bloco de quatro dígitos
		        v=v.replace(/(\d{4})(\d)/,"$1-$2");
		    }
			return v;
		}
	/* ]]> */	