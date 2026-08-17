package io.angellsim.snakegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.Preferences;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class SnakeGame extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont fontGameOver;
    private BitmapFont fontInstruction;
    private LinkedList<Vector2> snake; // Uma lista onde o primeiro item é a cabeça e o último é o rabo
    private Vector2 apple; // Posição (x, y) da maçã
    private Vector2 direction; // Para onde a cobra está indo (direção confirmada no último tick)
    private Vector2 nextDirection; // Próxima direção solicitada pelo jogador (buffer de input)
    private final int TILE_SIZE = 20; // O tamanho de cada "quadradinho" da grade em pixels
    private float timer = 0; // Para controlar a velocidade da cobra
    private boolean isGameOver = false; // verifica se o player perdeu o jogo
    private boolean hasGameStarted = false;
    private int score = 0; // qtd de pontos q ele fez
    private int highScore = 0; // maior qtd de pontos q ele fez
    private BitmapFont fontScore;
    private Preferences prefs;

    // Sistema de Boost e Texto Flutuante
    private int normalApplesEaten = 0;
    private int boostApplesLeft = 0;
    private String floatingText = "";
    private float floatingTextX = 0;
    private float floatingTextY = 0;
    private float floatingTextTimer = 0;

    @Override
    public void create() {
        // Ao invés de imagens complexas, usamos ShapeRenderer para desenhar quadrados
        // sólidos
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        // Carrega a fonte 8-bits
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PressStart2P.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        // Fonte grande para o GAME OVER
        parameter.size = 40;
        fontGameOver = generator.generateFont(parameter);

        // Fonte menor para a instrução
        parameter.size = 16;
        fontInstruction = generator.generateFont(parameter);

        // Fonte para o placar reduzida
        parameter.size = 12;
        fontScore = generator.generateFont(parameter);

        generator.dispose(); // Descarta o gerador após criar as fontes

        // Inicializa as preferências e carrega o recorde salvo
        prefs = Gdx.app.getPreferences("SnakeGamePrefs");
        highScore = prefs.getInteger("highscore", 0);

        initGame();
    }

    private void initGame() {
        // Inicializa a cobra com apenas 1 pedaço no centro do mapa (posição 10, 10 da
        // nossa grade)
        snake = new LinkedList<>();
        snake.add(new Vector2(10, 10));

        // Começa movendo para a direita (x = 1, y = 0)
        direction = new Vector2(1, 0);
        nextDirection = new Vector2(1, 0);

        // Coloca a maçã em uma posição aleatória inicial
        apple = new Vector2(15, 10);

        timer = 0;
        isGameOver = false;
        score = 0;
        normalApplesEaten = 0;
        boostApplesLeft = 0;
        floatingTextTimer = 0;
    }

    @Override
    public void render() {
        if (!hasGameStarted) {
            ScreenUtils.clear(0, 0, 0, 1);
            batch.begin();

            GlyphLayout layout = new GlyphLayout();

            // Desenha o Título no centro
            fontGameOver.setColor(Color.GREEN);
            layout.setText(fontGameOver, "SNAKE GAME");
            fontGameOver.draw(batch, "SNAKE GAME", (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f + 90);

            // Desenha a instrução
            fontInstruction.setColor(Color.WHITE);
            layout.setText(fontInstruction, "Pressione ESPACO para iniciar");
            fontInstruction.draw(batch, "Pressione ESPACO para iniciar", (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f + 10);

            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                hasGameStarted = true;
            }
            return;
        }

        if (isGameOver) {
            ScreenUtils.clear(0, 0, 0, 1);
            batch.begin();

            // Usamos GlyphLayout para calcular a largura exata de cada texto e centralizar
            // perfeitamente
            GlyphLayout layout = new GlyphLayout();

            // Desenha a mensagem de Game Over no centro e um pouco para cima
            fontGameOver.setColor(Color.RED);
            layout.setText(fontGameOver, "GAME OVER");
            fontGameOver.draw(batch, "GAME OVER", (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f + 90);

            // Desenha a instrução exatamente no meio
            fontInstruction.setColor(Color.WHITE);
            layout.setText(fontInstruction, "Pressione ESPACO para reiniciar");
            fontInstruction.draw(batch, "Pressione ESPACO para reiniciar",
                    (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() / 2f + 10);

            // Desenha a pontuação final e o recorde logo abaixo
            fontScore.setColor(Color.YELLOW);
            layout.setText(fontScore, "PONTOS: " + score);
            fontScore.draw(batch, "PONTOS: " + score, (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f - 40);

            layout.setText(fontScore, "RECORD: " + highScore);
            fontScore.draw(batch, "RECORD: " + highScore, (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f - 70);

            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                initGame();
            }
            return;
        }

        // --- LER COMANDOS DO JOGADOR ---
        // Armazena o próximo movimento solicitado em um buffer (nextDirection).
        // A validação de inversão usa sempre a `direction` confirmada do último tick,
        // evitando o bug de inversão causado por inputs muito rápidos entre dois ticks.
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && direction.y != -1)
            nextDirection.set(0, 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && direction.y != 1)
            nextDirection.set(0, -1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && direction.x != 1)
            nextDirection.set(-1, 0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && direction.x != -1)
            nextDirection.set(1, 0);

        // --- LÓGICA DE MOVIMENTO ---
        float deltaTime = Gdx.graphics.getDeltaTime();
        timer += deltaTime; // Acumula o tempo que passou desde o último frame

        // Animação do texto flutuante
        if (floatingTextTimer > 0) {
            floatingTextTimer -= deltaTime;
            floatingTextY += 50 * deltaTime; // Sobe o texto suavemente
        }

        if (timer > 0.05f) { // Só move a cada 0.05 segundos (dita a velocidade do jogo)
            timer = 0; // Zera o relógio

            // Aplica o próximo movimento bufferizado — só aqui a direção é confirmada
            direction.set(nextDirection);

            // Pega a posição atual da cabeça e calcula pra onde ela vai agora
            Vector2 head = snake.getFirst();
            Vector2 newHead = new Vector2(head.x + direction.x, head.y + direction.y);

            // --- LÓGICA DE ATRAVESSAR A PAREDE ---
            // Primeiro, calculamos qual é a última "casa" válida da nossa grade
            int maxGradeX = Gdx.graphics.getWidth() / TILE_SIZE;
            int maxGradeY = Gdx.graphics.getHeight() / TILE_SIZE;

            // Se saiu pela esquerda (menor que zero), aparece na extrema direita
            if (newHead.x < 0)
                newHead.x = maxGradeX - 1;
            // Se passou da extrema direita, aparece na esquerda (zero)
            else if (newHead.x >= maxGradeX)
                newHead.x = 0;

            // Se saiu por baixo (menor que zero), aparece no topo
            if (newHead.y < 0)
                newHead.y = maxGradeY - 1;
            // Se saiu pelo topo, aparece embaixo (zero)
            else if (newHead.y >= maxGradeY)
                newHead.y = 0;

            // Verifica se comeu a maçã
            boolean ateApple = newHead.equals(apple);

            if (!ateApple) {
                // Se NÃO comeu a maçã, removemos o último pedaço do rabo para a cobra andar sem
                // crescer infinitamente
                snake.removeLast();
            }

            // Verifica se bateu no próprio corpo
            if (snake.contains(newHead)) {
                isGameOver = true;

                // Atualiza e salva o recorde se a pontuação atual for maior
                if (score > highScore) {
                    highScore = score;
                    prefs.putInteger("highscore", highScore);
                    prefs.flush(); // Salva no disco imediatamente
                }
            }

            // Adiciona a nova cabeça na frente do corpo
            snake.addFirst(newHead);

            if (ateApple) {
                // Se comeu, sorteamos nova posição pra maçã em um local que não esteja ocupado pela cobra
                do {
                    apple.set(MathUtils.random(0, maxGradeX - 1), MathUtils.random(0, maxGradeY - 1));
                } while (snake.contains(apple));

                int pointsGained = 100;

                if (boostApplesLeft > 0) {
                    pointsGained = 150;
                    boostApplesLeft--;
                } else {
                    normalApplesEaten++;
                    if (normalApplesEaten == 5) {
                        // Após comer 5 normais, ativa o boost para as próximas 3
                        boostApplesLeft = 3;
                        normalApplesEaten = 0; // Reseta a contagem
                    }
                }

                score += pointsGained; // Aumenta a pontuação

                // Prepara o texto flutuante no local da maçã
                floatingText = "+" + pointsGained;
                floatingTextX = newHead.x * TILE_SIZE;
                floatingTextY = newHead.y * TILE_SIZE + TILE_SIZE; // Um pouco acima da cabeça
                floatingTextTimer = 1.0f; // Fica visível por 1 segundo
            }
        }

        // --- DESENHAR NA TELA ---
        ScreenUtils.clear(0, 0, 0, 1); // Fundo preto
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // Prepara para pintar formas preenchidas

        // 1. Pinta a maçã (Vermelha)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(apple.x * TILE_SIZE, apple.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // 2. Pinta a cobra (Verde)
        shapeRenderer.setColor(Color.GREEN);
        for (Vector2 part : snake) {
            shapeRenderer.rect(part.x * TILE_SIZE, part.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        shapeRenderer.end();

        // Desenha o placar (depois do ShapeRenderer, usa o SpriteBatch)
        batch.begin();
        fontScore.setColor(Color.WHITE);
        // Recuo ajustado de 140 para 180 para caber tranquilamente 5 dígitos (ex:
        // 10000)
        fontScore.draw(batch, "PONTOS: " + score, Gdx.graphics.getWidth() - 180, Gdx.graphics.getHeight() - 10);
        fontScore.draw(batch, "RECORD: " + highScore, Gdx.graphics.getWidth() - 180, Gdx.graphics.getHeight() - 30);

        // Desenha o texto flutuante se o timer estiver ativo
        if (floatingTextTimer > 0) {
            if (floatingText.equals("+150")) {
                fontScore.setColor(Color.GOLD);
            } else {
                fontScore.setColor(Color.GREEN);
            }
            fontScore.draw(batch, floatingText, floatingTextX, floatingTextY);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        fontGameOver.dispose();
        fontInstruction.dispose();
        fontScore.dispose();
    }
}
