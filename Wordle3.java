import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Scanner;

public class Wordle3 {

	public static void main(String[] args) {
		/*
		 * 1. El progrma lee los ficheros e importa el diccionario guardado y los datos
		 * de partidas anteriores 
		 * 2. Al empezar, sugiere una palabra aleatoria y espera
		 * a que el usuario le responda. ç
		 * 3. Durante 6 intentos o hasta que se acierte la
		 * palabra: - Se comprueba si se acertó la palabra. - Si no se acertó, evalúa
		 * las palabras de su diccionario y elige una nueva de entre las más afines. 
		 * 4. Si pierde, el programa pregunta cuál era la palabra oculta. 
		 * 5. Si la palabra oculta no figuraba en su diccionario, pregunta si se quiere añadir y actúa en
		 * consecuencia. 
		 * 6. Pregunta si el jugador desea seguir jugando.
		 */

		// Declaración de variables
		final int INTENTOSMAX = 6, NLETRAS = 5;
		final String CORRECTO = "22222";

		File diccFile = new File("Diccionario"), gameFile = new File("Partida");
		String[] diccionario = { "ALMAS", "ARBOL", "FRESA", "DUCHA", "PIANO", "BOTON", "SILLA", "LLAVE", "RATON",
				"SUELO", "METAL", "PAPEL", "NOCHE", "PRADO", "RASGO", "AMIGO", "MARCA", "LUNES", "HABIA", "NADAR" };
		String palabra, resultado, siNo, msgWrong,Pregunta;
		char[] palabra2 = new char[NLETRAS], letras0, letras1, palabra2T, letras0T, letras1T;
		char[][] palabra0 = new char[NLETRAS][0], palabra0T;
		int[] match;
		int contadorPalabras = 20, intentos = 0, partidasJugadas = 0, partidasGanadas = 0, max, nWrong;
		boolean wrong;

		Random rand = new Random();
		Scanner in = new Scanner(System.in);

		// 1. CREATE/IMPORT
		// Diccionario: Crea un nuevo diccionario o carga el existente. En caso de error
		// se juega con uno temporal.
		try {
			if (diccFile.createNewFile()) {
				System.out.println("Creando diccionario...");
				FileWriter diccNew = new FileWriter("Diccionario");
				String words = "" + contadorPalabras;
				for (int i = 0; i < diccionario.length; i++) {
					words = words + "\n" + diccionario[i];
				}
				diccNew.write(words);
				diccNew.close();
			} else {
				System.out.println("Cargando diccionario...");
				try (Scanner sc = new Scanner(diccFile)) {
					contadorPalabras = sc.nextInt();
					diccionario = new String[contadorPalabras];
					sc.nextLine();
					for (int i = 0; i < diccionario.length; i++) {
						diccionario[i] = sc.nextLine();
					}
				} catch (FileNotFoundException e1) {
					System.out.println("No se pudo cargar el diccionario. Jugando con diccionario temporal.");
					e1.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No se ha podido crear el diccionario. Jugando con diccionario temporal.");
		}

		// Partida: Crea la partida o carga la existente. En caso de error no se podrán
		// guardar los datos.
		try {
			if (gameFile.createNewFile()) {
				System.out.println("Creando nueva partida...");
				FileWriter diccNew = new FileWriter("Partida");
				diccNew.write("000 000\nNo deberías estar aquí.");
				diccNew.close();
			} else {
				System.out.println("Cargando partida...");
				try (Scanner sc = new Scanner(gameFile)) {
					partidasJugadas = sc.nextInt();
					partidasGanadas = sc.nextInt();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					System.out.println("No se pudo cargar la partida. Jugando como invitado.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No se pudo crear la partida. Jugando como invitado.");
		}

		if (partidasJugadas == 0) {
			System.out
					.println("\nPartidas jugadas: " + partidasJugadas + "\nPartidas que he ganado: " + partidasGanadas);
		} else {
			System.out.println("\nPartidas jugadas: " + partidasJugadas + "\nPartidas que he ganado: " + partidasGanadas
					+ "\nPorcentaje de victorias: " + (partidasGanadas * 100 / partidasJugadas) + "%");
		}

		do {
			intentos = 0;
			nWrong = 0;
			partidasJugadas++;
			letras0 = new char[0];
			letras1 = new char[0];
			for (int i = 0; i < NLETRAS; i++) {
				palabra2[i] = '0';
			}
			palabra0 = new char[NLETRAS][0];
			
			do {
			System.out.println("\nAntes de empezar ¿Sabes jugar wordle?");
			Pregunta = in.nextLine();
			while (!Pregunta.equalsIgnoreCase("Si") && !Pregunta.equalsIgnoreCase("No")) {
				System.out.println(
						"Escribe 'SI' empezar partida o 'NO' para aprender como jugar.");
				Pregunta = in.nextLine();
			}
			if (Pregunta.equalsIgnoreCase("No")) {
				System.out.println("\nNo sabes jugar, tú tranquil@ aquí te enseño como funciona ;)\n\nEste juego funciona prácticamente como el wordle normal salvo en que ERES TÚ el que piensa la palabra. \nY el programa intentará adivinarla.");
				System.out.println("\nSerás tu el que mediante los números 0 (letra errónea), 1 (letra correcta pero en mala posición)\ny 2 (Letra correcta en la posición correcta) irás valorando las respuestas del programa el cual \nintentará adivinar tu palabra.");
				System.out.println("\n¿Entendiste las normas?");
				Pregunta = in.nextLine();
			}
			if (Pregunta.equalsIgnoreCase("No")) {
				System.out.println("Si no te has enterado te recomiendo buscar mas información de como funciona en Google y que lo intentes más tarde.");
			}
			} while(Pregunta.equalsIgnoreCase("No"));
			
			// 2. START
			System.out.println("\nPERFECTO!!!\nJuguemos a Wordle.\nPiensa una palabra...\nY dime si la acierto:");
			palabra = diccionario[rand.nextInt(0, diccionario.length)];

			// 3. GAME
			do {
				msgWrong = "La entrada no es correcta.\nEscriba una serie de " + NLETRAS
						+ " digitos formada por 0, 1 y 2.";
				System.out.println(palabra);
				match = new int[diccionario.length];
				do {
					wrong = false;
					resultado = in.nextLine();
					while (resultado.length() != NLETRAS) {
						System.out.println(msgWrong);
						System.out.println(palabra);
						resultado = in.nextLine();
					}
					// Evaluación del resultado
					palabra2T = palabra2;
					letras0T = new char[0];
					letras1T = new char[0];
					palabra0T = palabra0;
					// Pasada de 2
					for (int i = 0; i < NLETRAS; i++) {
						char a = palabra.charAt(i);
						if (resultado.charAt(i) == '2') {
							if (presente(letras0, a) || presente(palabra0T[i], a)
									|| (palabra2T[i] != a && palabra2T[i] != '0')) {
								wrong = true;
								msgWrong = "La entrada no es coherente. Vuelva a introducirla (Error en posición: "
										+ (i + 1) + ").";
							} else {
								letras1T = agregarLetra(letras1T, a);
								if (palabra2T[i] != a)
									palabra2T[i] = a;
							}
						}
					}
					// Pasada de 0 y 1
					for (int i = 0; i < NLETRAS; i++) {
						char a = palabra.charAt(i);
						switch (resultado.charAt(i)) {
						case '0':
							if (presente(letras0, a) || presente(letras0T, a)) {
								if (!presente(letras0T, a))
									letras0T = agregarLetra(letras0T, a);
								if (!presente(palabra0T[i], a))
									palabra0T[i] = agregarLetra(palabra0T[i], a);
							} else {
								if ((presente(letras1, a) && !presente(letras1T, a)) || palabra2[i] == a) {
									wrong = true;
									msgWrong = "La entrada no es coherente. Vuelva a introducirla (Error en posición: "
											+ (i + 1) + ").";
								} else if (presente(letras1, a) || presente(letras1T, a)) {
									if (!presente(palabra0T[i], a))
										palabra0T[i] = agregarLetra(palabra0T[i], a);
								} else {
									letras0T = agregarLetra(letras0T, a);
									if (!presente(palabra0T[i], a))
										palabra0T[i] = agregarLetra(palabra0T[i], a);
								}
							}
							break;
						case '1':
							if (presente(letras0, a) || presente(letras0T, a) || palabra2[i] == a) {
								wrong = true;
								msgWrong = "La entrada no es coherente. Vuelva a introducirla (Error en posición: "
										+ (i + 1) + ").";
							} else {
								letras1T = agregarLetra(letras1T, a);
								if (!presente(palabra0T[i], a))
									palabra0T[i] = agregarLetra(palabra0T[i], a);
							}
							break;
						case '2':
							break;
						default:
							wrong = true; // Si el resultado contiene un caracter distinto de 0, 1 y 2, resultado
											// incorrecto.
							break;
						}
					}
					if (wrong) {
						if (nWrong < 2) {
							nWrong++;
							System.out.println(msgWrong);
							System.out.println("Errores restantes: " + (2 - nWrong) + "\n");
							System.out.println(palabra);
						} else {
							System.out.println("Has hecho trampa >:c\n¡He ganado!\n");
							if (resultado.equals(CORRECTO)) partidasGanadas++;
							wrong = false;
						}
					} else if (resultado.equalsIgnoreCase(CORRECTO)) {
						System.out.println("¡He ganado!");
						partidasGanadas++;
					} else {
						letras0 = juntarListas(letras0, letras0T);
						letras1 = juntarListas(letras1, letras1T);
						palabra0 = palabra0T;
						palabra2 = palabra2T;
						// Revisa las palabras del diccionario y les da un puntaje a cada una
						for (int i = 0; i < diccionario.length; i++) { // Para cada palabra del diccionario
							palabra = diccionario[i];
							for (int j = 0; j < NLETRAS; j++) { // Para cada letra de la palabra
								char a = palabra.charAt(j);
								if (palabra2[j] != '0') {
									if (a == palabra2[j]) {
										match[i] += 10; // Letra requerida correcta
									} else {
										match[i] -= 10; // Letra requerida incorrecta
									}
								}
								for (int k = 0; k < letras0.length; k++) { // Para cada letra prohibida
									if (a == letras0[k])
										match[i] -= 15; // Letra prohibida
								}
								for (int k = 0; k < letras1.length; k++) { // Para cada letra presente
									if (a == letras1[k]) {
										match[i] += 5; // Letra presente
										for (int l = 0; l < palabra0[j].length; l++) {
											if (a == palabra0[j][l])
												match[i] -= 10; // Letra presente, sitio incorrecto
										}
									}
								}
							}
						}
						// Comprueba el puntaje máximo y selecciona la primera palabra con ese puntaje
						max = match[0];
						for (int i = 0; i < match.length; i++) {
							if (match[i] > max)
								max = match[i];
						}
						boolean seguir = false;
						int i = 0;
						do {
							if (match[i] == max) {
								palabra = diccionario[i];
								seguir = true;
							}
							i++;
						} while (seguir == false);
					}
				} while (wrong == true);
				intentos++;
			} while (!resultado.equalsIgnoreCase(CORRECTO) && intentos < INTENTOSMAX && nWrong<2);

			// 4. PARTIDA PERDIDA
			if (!resultado.equals(CORRECTO)) {
				System.out.println("¿Cuál era la palabra oculta?");
				palabra = in.nextLine().toUpperCase();
				while (palabra.length() != NLETRAS) {
					System.out.println("Esa no es una palabra de 5 letras >:c");
					palabra = in.nextLine().toUpperCase();
				}
				// Evaluación del resultado
				nWrong = 0;
				wrong = false;
				do {
					wrong =false;
					int i = 0;
					do {
						char a = palabra.charAt(i);
						if (presente(letras0, a) || presente(palabra0T[i], a) || (palabra2T[i] != a && palabra2T[i] != '0')) {
							wrong = true;
							msgWrong = "La entrada no es coherente. Vuelva a introducirla (Error en posición: "
									+ (i + 1) + ").";
						}
						i++;
					} while (i < NLETRAS && !wrong);
					if (wrong) {
						if (nWrong < 2) {
							nWrong++;
							System.out.println(msgWrong +"Errores restantes: " + (2-nWrong));
							palabra = in.nextLine().toUpperCase();
						} else {
							System.out.println("La entrada no es coherente... Bueno, mejor déjalo. x_x");
							partidasGanadas++;
							wrong = false;
						}
					} else {
						if (existe(palabra, diccionario) == false) {
							System.out.println("No conocía esta palabra. ¿Quieres añadirla al diccionario?");
							siNo = in.nextLine();
							while (!siNo.equalsIgnoreCase("Si") && !siNo.equalsIgnoreCase("No")) {
								System.out.println(
										"Escribe 'SI' si quieres añadirla al diccionario o 'NO' si no quieres.");
								siNo = in.nextLine();
							}
							if (siNo.equalsIgnoreCase("Si")) {
								contadorPalabras++;
								diccionario = actualizarDiccionario(diccionario, contadorPalabras, palabra, diccFile);
							}
						}
					}
				} while (wrong);
			}

			// 5. ¿VOLVER A JUGAR?
			guardarPartida(partidasJugadas, partidasGanadas, gameFile);
			System.out.println("\nPartidas jugadas: " + partidasJugadas + "\nPartidas que he ganado: " + partidasGanadas
					+ "\nPorcentaje de victorias: " + (partidasGanadas * 100 / partidasJugadas) + "%");
			System.out.println("\n¿Quieres seguir jugando?");
			siNo = in.nextLine();
			while (!siNo.equalsIgnoreCase("Si") && !siNo.equalsIgnoreCase("No")) {
				System.out.println("Escribe 'SI' si quieres seguir jugando o 'NO' si quieres dejar de jugar.");
				siNo = in.nextLine();
			}

		} while (siNo.equalsIgnoreCase("Si"));
	}

	public static boolean presente(char[] contenedor, char a) {
		boolean presente = false;
		for (int i = 0; i < contenedor.length; i++) {
			if (a == contenedor[i]) {
				presente = true;
			}
		}
		return presente;
	}

	public static char[] agregarLetra(char[] letrasAntiguas, char a) {
		char[] letrasAmpliadas = new char[letrasAntiguas.length + 1];
		for (int i = 0; i < letrasAntiguas.length; i++) {
			letrasAmpliadas[i] = letrasAntiguas[i];
		}
		letrasAmpliadas[letrasAntiguas.length] = a;
		return letrasAmpliadas;
	}

	public static char[] eliminarLetra(char[] letrasAntiguas, char a) {
		char[] letrasReducidas = new char[letrasAntiguas.length - 1];
		for (int i = 0; i < letrasReducidas.length; i++) {
			if (letrasAntiguas[i] != a) {
				letrasReducidas[i] = letrasAntiguas[i];
			}
		}
		return letrasReducidas;
	}

	public static char[] juntarListas(char[] lista, char[] listaAgregada) {
		for (int i = 0; i < listaAgregada.length; i++) {
			if (!presente(lista, listaAgregada[i]))
				lista = agregarLetra(lista, listaAgregada[i]);
		}
		return lista;
	}

	public static boolean existe(String palabra, String[] diccionario) {
		boolean contiene = false;
		for (int i = 0; i < diccionario.length; i++) {
			if (diccionario[i].equals(palabra))
				contiene = true;
		}
		return contiene;
	}

	public static void guardarPartida(int partidasJugadas, int partidasGanadas, File gameFile) {
		String stats = "", partidas;
		partidas = "" + partidasJugadas;
		if (partidas.length() > 3) {
			stats = stats + 999;
		} else {
			while (partidas.length() < 3) {
				partidas = 0 + partidas;
			}
			stats = partidas;
		}
		partidas = "" + partidasGanadas;
		if (partidas.length() > 3) {
			stats = stats + " " + 999;
		} else {
			while (partidas.length() < 3) {
				partidas = 0 + partidas;
	
		}
			stats = stats + " " + partidas;
	}
		try {
			RandomAccessFile raf = new RandomAccessFile("Partida", "rw");
			raf.seek(0);
			raf.write(stats.getBytes());
			raf.close();
		} catch (IOException e) {
			System.out.println("No se pudo guardar la partida");
			e.printStackTrace();
		}
	}

	public static String[] actualizarDiccionario(String[] diccionarioAntiguo, int contadorPalabras, String palabraNueva,
			File diccFile) {
		String[] diccionarioActualizado = new String[contadorPalabras];
		for (int i = 0; i < diccionarioAntiguo.length; i++) {
			diccionarioActualizado[i] = diccionarioAntiguo[i];
		}
		diccionarioActualizado[contadorPalabras - 1] = palabraNueva;
		try (FileWriter diccNew = new FileWriter(diccFile);) {
			String words = "" + contadorPalabras;
			for (int i = 0; i < diccionarioActualizado.length; i++) {
				words = words + "\n" + diccionarioActualizado[i];
			}
			diccNew.write(words);
			diccNew.close();
			System.out.println("Diccionario guardado con éxito.");
		} catch (IOException e) {
			System.out.println("No se ha podido guardar el diccionario.");
			e.printStackTrace();
			return diccionarioAntiguo;
		}
		return diccionarioActualizado;
	}

}
