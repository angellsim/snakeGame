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

    // --- PIXEL ART MAPS (8x8 grid, cada célula = bloco 4x4 px no tile 32x32) ---
    // Maçã: 0=bg, 1=vermelho escuro, 2=vermelho, 3=highlight (animado), 4=folha
    // verde, 5=cabo marrom
    private static final int[][] APPLE_MAP = {
            { 0, 0, 0, 5, 4, 0, 0, 0 },
            { 0, 0, 0, 5, 4, 4, 0, 0 },
            { 0, 1, 1, 2, 2, 2, 1, 0 },
            { 1, 2, 2, 3, 2, 2, 2, 1 },
            { 1, 2, 3, 2, 2, 2, 2, 1 },
            { 1, 2, 2, 2, 2, 2, 1, 0 },
            { 0, 1, 2, 2, 2, 1, 0, 0 },
            { 0, 0, 1, 1, 1, 0, 0, 0 },
    };
    // Corpo da cobra: 0=border escuro, 1=verde corpo, 2=highlight claro
    private static final int[][] BODY_MAP = {
            { 0, 0, 1, 1, 1, 1, 0, 0 },
            { 0, 1, 2, 1, 1, 1, 1, 0 },
            { 1, 1, 2, 2, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 0, 1, 1 },
            { 1, 1, 0, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 2, 2, 1, 1 },
            { 0, 1, 1, 1, 1, 2, 1, 0 },
            { 0, 0, 1, 1, 1, 1, 0, 0 },
    };

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont fontGameOver;
    private BitmapFont fontInstruction;
    private LinkedList<Vector2> snake; // Uma lista onde o primeiro item é a cabeça e o último é o rabo
    private Vector2 apple; // Posição (x, y) da maçã
    private Vector2 direction; // Para onde a cobra está indo (direção confirmada no último tick)
    private Vector2 nextDirection; // Próxima direção solicitada pelo jogador (buffer de input)
    private final int TILE_SIZE = 32; // O tamanho de cada "quadradinho" da grade em pixels
    private int gridCols; // Quantidade de colunas da grade (calculado em create())
    private int gridRows; // Quantidade de linhas da grade (calculado em create())
    private float timer = 0; // Para controlar a velocidade da cobra
    private boolean isGameOver = false; // verifica se o player perdeu o jogo
    private boolean isVictory = false; // verifica se o player ganhou o jogo
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
    private float skinTimer = 0f; // Timer contínuo para animações visuais (maçã pulsante, etc.)

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

        // Calcula as dimensões da grade uma única vez, com base no tamanho real da
        // janela.
        // Usar campos garante que wrapping e spawn da maçã nunca divergem.
        gridCols = Gdx.graphics.getWidth() / TILE_SIZE; // ex: 640 / 32 = 20
        gridRows = Gdx.graphics.getHeight() / TILE_SIZE; // ex: 480 / 32 = 15

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
        isVictory = false;
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

            GlyphLayout layout = new GlyphLayout();

            if (isVictory) {
                fontGameOver.setColor(Color.GOLD);
                layout.setText(fontGameOver, "VICTORY");
                fontGameOver.draw(batch, "VICTORY", (Gdx.graphics.getWidth() - layout.width) / 2f,
                        Gdx.graphics.getHeight() / 2f + 90);

                fontScore.setColor(Color.WHITE);
                layout.setText(fontScore, "PONTOS: " + score);
                fontScore.draw(batch, "PONTOS: " + score, (Gdx.graphics.getWidth() - layout.width) / 2f,
                        Gdx.graphics.getHeight() / 2f + 40);

                fontInstruction.setColor(Color.WHITE);
                layout.setText(fontInstruction, "Pressione ESPACO para reiniciar");
                fontInstruction.draw(batch, "Pressione ESPACO para reiniciar",
                        (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() / 2f - 10);
                
                layout.setText(fontInstruction, "Pressione ESC para sair");
                fontInstruction.draw(batch, "Pressione ESC para sair",
                        (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() / 2f - 40);

                batch.end();

                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    initGame();
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit();
                }
                return;
            }

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

            layout.setText(fontInstruction, "Pressione ESC para sair");
            fontInstruction.draw(batch, "Pressione ESC para sair",
                    (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() / 2f - 20);

            // Desenha a pontuação final e o recorde logo abaixo
            fontScore.setColor(Color.YELLOW);
            layout.setText(fontScore, "PONTOS: " + score);
            fontScore.draw(batch, "PONTOS: " + score, (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f - 50);

            layout.setText(fontScore, "RECORD: " + highScore);
            fontScore.draw(batch, "RECORD: " + highScore, (Gdx.graphics.getWidth() - layout.width) / 2f,
                    Gdx.graphics.getHeight() / 2f - 80);

            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                initGame();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
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
        skinTimer += deltaTime; // Avança o timer de animações visuais

        // Animação do texto flutuante
        if (floatingTextTimer > 0) {
            floatingTextTimer -= deltaTime;
            floatingTextY += 50 * deltaTime; // Sobe o texto suavemente
        }

        if (timer > 0.07f) { // Só move a cada 0.05 segundos (dita a velocidade do jogo)
            timer = 0; // Zera o relógio

            // Aplica o próximo movimento bufferizado — só aqui a direção é confirmada
            direction.set(nextDirection);

            // Pega a posição atual da cabeça e calcula pra onde ela vai agora
            Vector2 head = snake.getFirst();
            Vector2 newHead = new Vector2(head.x + direction.x, head.y + direction.y);

            // --- LÓGICA DE ATRAVESSAR A PAREDE ---
            // Usa gridCols/gridRows (calculados em create()) como fonte única de verdade.

            // Se saiu pela esquerda (menor que zero), aparece na extrema direita
            if (newHead.x < 0)
                newHead.x = gridCols - 1;
            // Se passou da extrema direita, aparece na esquerda (zero)
            else if (newHead.x >= gridCols)
                newHead.x = 0;

            // Se saiu por baixo (menor que zero), aparece no topo
            if (newHead.y < 0)
                newHead.y = gridRows - 1;
            // Se saiu pelo topo, aparece embaixo (zero)
            else if (newHead.y >= gridRows)
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
                // Se comeu, sorteamos nova posição pra maçã em um local que não esteja ocupado
                // pela cobra
                do {
                    // gridCols-1 e gridRows-1 garantem que a maçã nunca cai fora do campo visível
                    apple.set(MathUtils.random(0, gridCols - 1), MathUtils.random(0, gridRows - 1));
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

                if (score >= 5000) {
                    isVictory = true;
                    isGameOver = true;
                    if (score > highScore) {
                        highScore = score;
                        prefs.putInteger("highscore", highScore);
                        prefs.flush(); // Salva no disco imediatamente
                    }
                }

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

        // --- PIXEL ART RENDERING ---
        // Cada tile 32x32 é dividido em grade 8x8; cada célula = bloco P×P pixels
        final int P = 4; // tamanho em pixels de cada "pixel" da arte
        final int G = 8; // quantidade de células por lado da grade

        // 1. Maçã — mapa de pixels com highlight piscando
        // O highlight (código 3) alterna entre rosa e branco usando skinTimer
        boolean highlightOn = ((int) (skinTimer * 3f) % 2) == 0;
        float ax = apple.x * TILE_SIZE;
        float ay = apple.y * TILE_SIZE;
        for (int row = 0; row < G; row++) {
            // row=0 do array = topo visual → renderiza no topo do tile (y maior)
            float ry = ay + (G - 1 - row) * P;
            for (int col = 0; col < G; col++) {
                int code = APPLE_MAP[row][col];
                switch (code) {
                    case 0:
                        continue; // transparente
                    case 1:
                        shapeRenderer.setColor(0.50f, 0.04f, 0.04f, 1f);
                        break; // vermelho escuro
                    case 2:
                        shapeRenderer.setColor(0.88f, 0.14f, 0.14f, 1f);
                        break; // vermelho
                    case 3: // highlight piscante
                        if (highlightOn)
                            shapeRenderer.setColor(1.00f, 0.75f, 0.75f, 1f);
                        else
                            shapeRenderer.setColor(0.95f, 0.50f, 0.50f, 1f);
                        break;
                    case 4:
                        shapeRenderer.setColor(0.10f, 0.60f, 0.10f, 1f);
                        break; // folha
                    case 5:
                        shapeRenderer.setColor(0.40f, 0.22f, 0.05f, 1f);
                        break; // cabo
                    default:
                        continue;
                }
                shapeRenderer.rect(ax + col * P, ry, P, P);
            }
        }

        // 2. Cobra — cabeça e corpo em pixel art
        boolean isHead = true;
        for (Vector2 part : snake) {
            float px = part.x * TILE_SIZE;
            float py = part.y * TILE_SIZE;

            if (isHead) {
                // --- CABEÇA: tile verde sólido pixel a pixel com borda ---
                for (int row = 0; row < G; row++) {
                    float ry = py + (G - 1 - row) * P;
                    for (int col = 0; col < G; col++) {
                        boolean border = (row == 0 || row == G - 1 || col == 0 || col == G - 1);
                        if (border)
                            shapeRenderer.setColor(0.05f, 0.45f, 0.05f, 1f);
                        else
                            shapeRenderer.setColor(0.28f, 0.92f, 0.28f, 1f);
                        shapeRenderer.rect(px + col * P, ry, P, P);
                    }
                }
                // Olhos: dois blocos 2×2 pixels; brilhinho branco no canto superior direito do
                // olho
                // A posição (em células da grade) depende da direção
                int e1c, e1r, e2c, e2r;
                if (direction.x == 1) { // → direita
                    e1c = 5;
                    e1r = 2;
                    e2c = 5;
                    e2r = 5;
                } else if (direction.x == -1) { // ← esquerda
                    e1c = 2;
                    e1r = 2;
                    e2c = 2;
                    e2r = 5;
                } else if (direction.y == 1) { // ↑ cima
                    e1c = 2;
                    e1r = 2;
                    e2c = 5;
                    e2r = 2;
                } else { // ↓ baixo
                    e1c = 2;
                    e1r = 5;
                    e2c = 5;
                    e2r = 5;
                }
                // Pupila preta (2×2 pixels)
                shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 1f);
                shapeRenderer.rect(px + e1c * P, py + (G - 1 - e1r) * P, P * 2, P * 2);
                shapeRenderer.rect(px + e2c * P, py + (G - 1 - e2r) * P, P * 2, P * 2);
                // Brilho branco no canto do olho (1×1 pixel)
                shapeRenderer.setColor(0.95f, 0.95f, 0.95f, 1f);
                shapeRenderer.rect(px + (e1c + 1) * P, py + (G - e1r) * P, P, P);
                shapeRenderer.rect(px + (e2c + 1) * P, py + (G - e2r) * P, P, P);
                isHead = false;

            } else {
                // --- CORPO: mapa BODY_MAP com padrão de escamas ---
                for (int row = 0; row < G; row++) {
                    float ry = py + (G - 1 - row) * P;
                    for (int col = 0; col < G; col++) {
                        int code = BODY_MAP[row][col];
                        switch (code) {
                            case 0:
                                shapeRenderer.setColor(0.04f, 0.35f, 0.04f, 1f);
                                break; // border
                            case 1:
                                shapeRenderer.setColor(0.12f, 0.65f, 0.12f, 1f);
                                break; // corpo
                            case 2:
                                shapeRenderer.setColor(0.22f, 0.80f, 0.22f, 1f);
                                break; // highlight
                            default:
                                continue;
                        }
                        shapeRenderer.rect(px + col * P, ry, P, P);
                    }
                }
            }
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
