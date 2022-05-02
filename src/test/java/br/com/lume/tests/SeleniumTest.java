package br.com.lume.tests;

import org.junit.Before;
import org.junit.After;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import java.util.Map;
import java.util.HashMap;
import java.time.Duration;

public class SeleniumTest {

	private final String TEST_URL_LOCAL = "http://localhost:8080/intelidente";
	private final String PATH_DRIVER_CHROME = "";										//"C:\\web-drivers\\chromedriver.exe"

	private static final String SYS_PARAM_HEADLESS = "headless";						// Par?etro para tivar e desativar uso de UI pelo teste
	private static final String SYS_PARAM_TEST_URL = "urlteste";						// Par?etro para definir url de teste
	private static final String SYS_PARAM_FINALIZAR = "finalizar";						// Par?etro para definir se navegar ira ser finalizado no fim do teste
	private static final String SYS_PARAM_CHROME_DRIVER = "webdriver.chrome.driver";	// Par?etro para definir 
	
	private static boolean setupFinalizado = false;
	private static boolean modoHeadless = false;
	private static boolean finalizarNavegador = true;
	private static String urlTeste = null;

	protected WebDriver driver;
	protected Map<String, Object> vars;
	protected JavascriptExecutor js;
	protected WebDriverWait wait;

	@Before
	public void setUp() {
		// Controle para executar setup apenas uma vez
		if (setupFinalizado) return;

		/* Captura de parâmetros */

		modoHeadless = "true".equals(System.getProperty(SYS_PARAM_HEADLESS));
		if (modoHeadless) System.out.println("Utilizando modo headless");

		String urlTesteParam = System.getProperty(SYS_PARAM_TEST_URL);
		urlTeste = urlTesteParam != null ? urlTesteParam : TEST_URL_LOCAL;
		System.out.println("Url de testes sendo utilizada: " + getTestURL());

		// Caso não especificado utilizar driver no caminho padrão
		String caminhoDriverChrome = System.getProperty(SYS_PARAM_CHROME_DRIVER);
		if (caminhoDriverChrome == null)
			System.setProperty("SYS_PARAM_CHROME_DRIVER", PATH_DRIVER_CHROME);
		
		// Caso esteja vazio presume-se estar no PATH do sistema
		if (caminhoDriverChrome != null && caminhoDriverChrome.length() <= 0)
			System.getProperties().remove(SYS_PARAM_CHROME_DRIVER);
		
		caminhoDriverChrome = System.getProperty(SYS_PARAM_CHROME_DRIVER);
		if (caminhoDriverChrome == null) System.out.println("Nenhuma localização especificada para driver do chrome");
		else System.out.println("Utilizando driver chrome localizado em" + caminhoDriverChrome);

		
		finalizarNavegador = !"false".equals(System.getProperty(SYS_PARAM_FINALIZAR));
		if (!finalizarNavegador) System.out.println("Finalizar navegador ao fim do teste desabilitado");

		/* Inicializações para ambientes de testes */
		ChromeOptions options = new ChromeOptions();
		if (modoHeadless)
			options.addArguments("--headless");				// Desabilita interface gráica 
		options.addArguments("--disable-infobars");			// Desabilita barras de alerta
		options.addArguments("--disable-extensions"); 		// Desabilita extensões
		options.addArguments("--disable-notifications");	// Desabilita alertas no navegador
		options.addArguments("--disable-dev-shm-usage"); 	// Desabilita limite de uso de recursos
		options.addArguments("--no-sandbox"); 				// Necessáio para executar chrome como usuáio root em linux
		driver = new ChromeDriver(options);

		js = (JavascriptExecutor) driver;
		vars = new HashMap<String, Object>();
		wait = new WebDriverWait(driver, Duration.ofMillis(1000));

		// Controle para executar setup apenas uma vez
		setupFinalizado = true;
	}

	@After
	public void tearDown() { 
		if (finalizarNavegador) driver.quit();
	}

	public String getTestURL(){
		return urlTeste;		
	}

	public Dimension getDimensionPadrao(){
		return new Dimension(1280, 820);
	}
}
