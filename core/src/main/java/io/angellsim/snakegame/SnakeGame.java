package io.angellsim.snakegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.LinkedList;

public class SnakeGame extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private LinkedList<Vector2> snake; // Uma lista onde o primeiro item é a cabeça e o último é o rabo
    private Vector2 apple; // Posição (x, y) da maçã
    private Vector2 direction; // Para onde a cobra está indo
    private final int TILE_SIZE = 20; // O tamanho de cada "quadradinho" da grade em pixels
    private float timer = 0; // Para controlar a velocidade da cobra

    @Override
    public void create() {
        // Ao invés de imagens complexas, usamos ShapeRenderer para desenhar quadrados sólidos
        shapeRenderer = new ShapeRenderer();
        
        // Inicializa a cobra com apenas 1 pedaço no centro do mapa (posição 10, 10 da nossa grade)
        snake = new LinkedList<>();
        snake.add(new Vector2(10, 10));
        
        // Começa movendo para a direita (x = 1, y = 0)
        direction = new Vector2(1, 0);
        
        // Coloca a maçã em uma posição aleatória inicial
        apple = new Vector2(15, 10);
    }

    @Override
    public void render() {
        // --- LER COMANDOS DO JOGADOR ---
        // Se apertar pra cima e não estiver descendo, vai pra cima
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && direction.y != -1) direction.set(0, 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) && direction.y != 1) direction.set(0, -1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && direction.x != 1) direction.set(-1, 0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && direction.x != -1) direction.set(1, 0);

        // --- LÓGICA DE MOVIMENTO ---
        timer += Gdx.graphics.getDeltaTime(); // Acumula o tempo que passou desde o último frame
        if (timer > 0.15f) { // Só move a cada 0.15 segundos (dita a velocidade do jogo)
            timer = 0; // Zera o relógio
            
            // Pega a posição atual da cabeça e calcula pra onde ela vai agora
            Vector2 head = snake.getFirst();
            Vector2 newHead = new Vector2(head.x + direction.x, head.y + direction.y);
            
            // Adiciona a nova cabeça na frente do corpo
            snake.addFirst(newHead);
            
            // Verifica se comeu a maçã
            if (newHead.equals(apple)) {
                // Se comeu, não removemos o rabo (a cobra cresce!) e sorteamos nova posição pra maçã
                int maxGradeX = (Gdx.graphics.getWidth() / TILE_SIZE) - 1;
                int maxGradeY = (Gdx.graphics.getHeight() / TILE_SIZE) - 1;
                apple.set(MathUtils.random(0, maxGradeX), MathUtils.random(0, maxGradeY));
            } else {
                // Se NÃO comeu a maçã, removemos o último pedaço do rabo para a cobra andar sem crescer infinitamente
                snake.removeLast();
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
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
