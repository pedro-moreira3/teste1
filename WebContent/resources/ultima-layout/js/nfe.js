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
							'Quinta-feira', 'Sexta-feira', 'Sábado' ],
					dayNamesShort : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
							'Sáb' ],
					dayNamesMin : [ 'Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex',
							'Sáb' ],
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

			//masktel();

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
		
		
		/*<![CDATA[*/
		function mascaraMutuario(o,f){
		 v_obj=o;
		    v_fun=f;
		    setTimeout('execmascara()',1);
		}
		function execmascara(){
		    v_obj.value=v_fun(v_obj.value);
		}
		function documento(v){				
			 //Remove tudo o que não é dígito
		    v=v.replace(/\D/g,"");
		    var n = v.length;
		    if (n <= 11) {
		    	//Coloca um ponto entre o terceiro e o quarto dígitos
		        v=v.replace(/(\d{3})(\d)/,"$1.$2");
		        //Coloca um ponto entre o terceiro e o quarto dígitos
		        //de novo (para o segundo bloco de números)
		        v=v.replace(/(\d{3})(\d)/,"$1.$2");
		        //Coloca um hífen entre o terceiro e o quarto dígitos
		        v=v.replace(/(\d{3})(\d{1,2})$/,"$1-$2");
		    } else { //CNPJ
		        //Coloca ponto entre o segundo e o terceiro dígitos
		        v=v.replace(/^(\d{2})(\d)/,"$1.$2");
		        //Coloca ponto entre o quinto e o sexto dígitos
		        v=v.replace(/^(\d{2})\.(\d{3})(\d)/,"$1.$2.$3");				 
		        //Coloca uma barra entre o oitavo e o nono dígitos
		        v=v.replace(/\.(\d{3})(\d)/,".$1/$2");
		        //Coloca um hífen depois do bloco de quatro dígitos
		        v=v.replace(/(\d{4})(\d)/,"$1-$2");
		    }
			return v;
		}
	/*]]>*/	