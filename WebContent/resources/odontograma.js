//<![CDATA[			
var canvasSelecionado;
function desenhaDente(numDente) {
	var canvas = document.getElementById('canvasDente' + numDente);
	$('#canvasDente' + numDente).click(function(event) {
		processaClick(event, canvas, numDente);
	});
	if (canvas.getContext) {
		var ctx = canvas.getContext("2d");
		var tam = canvas.width;

		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.beginPath();
		desenhaTudo(ctx, tam);
		ctx.closePath();
	}
}

function clearCanvas(pos, numDente, ctx, tam) {
	// console.log('clearCanvas ' + numDente + ' ' + canvasSelecionado.id);
	canvasSelecionado.width = canvasSelecionado.width;
	desenhaTudo(ctx, tam);
	/***************************************************************************
	 * for(x=0;x < $.myglobals.odontograma.length;x++){ var oOdont =
	 * $.myglobals.odontograma[x]; if(oOdont.posicao != pos &&
	 * oOdont.dente.descricao == numDente){ //console.log('clearCanvas1 ' +
	 * oOdont.dente.descricao); $.myglobals.odontograma[x] = ''; } }
	 **************************************************************************/

}

function desenhaTudo(ctx, tam) {
	desenhaOeste(ctx, tam);
	desenhaSul(ctx, tam);
	desenhaNorte(ctx, tam);
	desenhaLeste(ctx, tam);
	desenhaCentro(ctx, tam);
}

function desenhaOeste(ctx, tam) {
	ctx.moveTo(0, 0);
	ctx.lineTo(0, tam);
	ctx.lineTo(tam / 4, tam - (tam / 4));
	ctx.lineTo(tam / 4, tam / 4);
	ctx.lineTo(0, 0);
	ctx.stroke();
}

function desenhaSul(ctx, tam) {
	ctx.moveTo(0, tam);
	ctx.lineTo(tam, tam);
	ctx.lineTo(tam - (tam / 4), tam - (tam / 4));
	ctx.lineTo(tam / 4, tam - (tam / 4));
	ctx.lineTo(0, tam);
	ctx.stroke();
}

function desenhaNorte(ctx, tam) {
	ctx.moveTo(0, 0);
	ctx.lineTo(tam, 0);
	ctx.lineTo(tam - (tam / 4), tam / 4);
	ctx.lineTo(tam / 4, tam / 4);
	ctx.lineTo(0, 0);
	ctx.stroke();
}

function desenhaLeste(ctx, tam) {
	ctx.moveTo(tam, tam);
	ctx.lineTo(tam, 0);
	ctx.lineTo(tam - (tam / 4), tam / 4);
	ctx.lineTo(tam - (tam / 4), tam - (tam / 4));
	ctx.lineTo(tam, tam);
	ctx.stroke();
}

function desenhaCentro(ctx, tam) {
	ctx.moveTo(tam / 4, tam / 4);
	ctx.lineTo(tam / 4, tam - (tam / 4));
	ctx.lineTo(tam - (tam / 4), tam - (tam / 4));
	ctx.lineTo(tam - (tam / 4), tam / 4);
	ctx.lineTo(tam / 4, tam / 4);
	ctx.stroke();
}

function processaClick(event, canvas, numDente) {
	alert.log('processaClick ' + numDente);
	canvasSelecionado = canvas;
	var ctx = canvas.getContext("2d");
	var rect = canvas.getBoundingClientRect();
	var tam = canvas.width;

	var x = event.clientX - rect.left;
	var y = event.clientY - rect.top;

	ctx.beginPath();
	desenhaOeste(ctx, tam);
	setProblema(ctx, x, y, 'O', numDente);
	ctx.closePath();

	ctx.beginPath();
	desenhaSul(ctx, tam);
	setProblema(ctx, x, y, 'S', numDente);
	ctx.closePath();

	ctx.beginPath();
	desenhaNorte(ctx, tam);
	setProblema(ctx, x, y, 'N', numDente);
	ctx.closePath();

	ctx.beginPath();
	desenhaLeste(ctx, tam);
	setProblema(ctx, x, y, 'L', numDente);
	ctx.closePath();

	ctx.beginPath();
	desenhaCentro(ctx, tam);
	setProblema(ctx, x, y, 'C', numDente);
	ctx.closePath();

}

function setProblema(ctx, x, y, pos, numDente) {
	var problema = $('input[name=lume:problema]:checked').val();
	var tam = $('#canvasDente' + numDente).width();
	if (ctx.isPointInPath(x, y)) {
		// Se o problema for de preenchimento central forcar C no pos
		if (problema != 'CA' && problema != 'RA' && problema != 'CL'
				&& problema != 'RD') {
			pos = 'C';
		}
		var alterar = setOdontograma(pos, numDente, problema);
		// console.log(alterar);
		if (alterar) {
			doProblema(problema, ctx, pos, tam, numDente, true);
		}
	} else {
		ctx.stroke();
	}
}

function doProblema(problema, ctx, pos, tam, numDente, clear) {
	// console.log(problema + ' ' + pos + ' ' + numDente);
	if (problema == 'CA') {
		ctx.fillStyle = "#FF0000";
		ctx.fill();
	} else if (problema == 'RA') {
		ctx.fillStyle = "#0000FF";
		ctx.fill();
	} else if (problema == 'CL') {
		ctx.fillStyle = "#00FF00";
		ctx.fill();
	} else if (problema == 'RD') {
		ctx.fillStyle = "#FFFF00";
		ctx.fill();
	} else if (problema == 'RR') {
		if (clear) {
			clearCanvas(pos, numDente, ctx, tam);
		}
		ctx.fillStyle = "#000000";
		ctx.font = (tam / 4) + "pt Calibri";
		ctx.fillText("RR", tam / 3, (tam / 5) * 3);
	} else if (problema == 'RG') {
		if (clear) {
			clearCanvas(pos, numDente, ctx, tam);
		}
		ctx.fillStyle = "#000000";
		ctx.font = (tam / 4) + "pt Calibri";
		ctx.fillText("RG", tam / 3, (tam / 5) * 3);
	} else if (problema == 'PR') {
		if (clear) {
			clearCanvas(pos, numDente, ctx, tam);
		}
		ctx.fillStyle = "#000000";
		ctx.font = (tam / 4) + "pt Calibri";
		ctx.fillText("P", tam / 2.5, (tam / 5) * 3);
	} else if (problema == 'DA') {
		if (clear) {
			clearCanvas(pos, numDente, ctx, tam);
		}
		ctx.moveTo(0, 0);
		ctx.lineTo(tam, tam);
		ctx.moveTo(0, tam);
		ctx.lineTo(tam, 0);
		ctx.stroke();
	}
}

function setOdontograma(pos, numDente, problema) {
	// Se o problema for de preenchimento central limpar todas as posicoes
	// cadastradas
	if (problema != 'CA' && problema != 'RA' && problema != 'CL'
			&& problema != 'RD') {
		var aux = new Array;
		// console.log($.myglobals.odontograma);
		for (x = 0; x < $.myglobals.odontograma.length; x++) {
			var oOdont = $.myglobals.odontograma[x];
			if (oOdont.dente.descricao != numDente) {
				aux.push($.myglobals.odontograma[x]);
			}
		}
		$.myglobals.odontograma = aux;
		// console.log($.myglobals.odontograma);
	}

	// Verifica se vai substituir o valor JSON do dente no array ou vai inserir
	// um novo
	for (var x = 0; x < $.myglobals.odontograma.length; x++) {
		var oOdont = $.myglobals.odontograma[x];
		if (oOdont.posicao == pos && oOdont.dente.descricao == numDente) {
			if (oOdont.statusDente.sigla != problema) {
				// console.log(oOdont.posicao +' '+ pos +' '+
				// oOdont.dente.descricao +' '+ numDente +' '+
				// oOdont.statusDente.sigla +' '+ problema);
				oOdont.statusDente.sigla = problema;
				return true;
			} else {
				return false;
			}
		}
	}

	var oOdont = eval('({"statusDente":{"sigla":"' + problema
			+ '"},"posicao":"' + pos + '","dente":{"descricao":' + numDente
			+ '}})');
	$.myglobals.odontograma.push(oOdont);
	return true;
}

function loadProblema(numDente, pos, problema) {
	var canvas = document.getElementById('canvasDente' + numDente);
	var ctx = canvas.getContext("2d");
	var tam = canvas.width;

	ctx.beginPath();
	if (pos == "O") {
		desenhaOeste(ctx, tam);
	} else if (pos == "S") {
		desenhaSul(ctx, tam);
	} else if (pos == "N") {
		desenhaNorte(ctx, tam);
	} else if (pos == "L") {
		desenhaLeste(ctx, tam);
	} else if (pos == "C") {
		desenhaCentro(ctx, tam);
	} else {
		return;
	}

	doProblema(problema, ctx, pos, tam, numDente);
	ctx.closePath();
}

$.myglobals = {
	odontograma : []
};
function handleComplete(xhr, status, args) {
	if (args.validationFailed) {
		alert("Validation Failed");
	} else {
		$.myglobals.odontograma = JSON.parse(args.odontograma);

		// Redesenha Dentes
		for (var x = 11; x < 86; x++) {
			if ($("#canvasDente" + x).length > 0) {
				desenhaDente(x);
			}
		}

		for (var x = 0; x < $.myglobals.odontograma.length; x++) {
			var oOdont = $.myglobals.odontograma[x];
			var numDente = oOdont.dente.descricao;
			var pos = oOdont.posicao;
			var problema = oOdont.statusDente.sigla;
			loadProblema(numDente, pos, problema);
		}
	}
}

function persist() {
	pesistOdontograma({
		odontograma : JSON.stringify($.myglobals.odontograma),
	});
}
// ]]>
