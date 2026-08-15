# 🐍 Jogo da Cobrinha (Snake Game) em Java

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/LibGDX-CC3333?style=for-the-badge&logo=libgdx&logoColor=white)
![Status](https://img.shields.io/badge/Status-Conclu%C3%ADdo-success?style=for-the-badge)

Um clone moderno e polido do clássico **Jogo da Cobrinha**, desenvolvido do zero utilizando **Java** e a framework de jogos **LibGDX**. Este projeto foi construído com foco em arquitetura limpa, performance (renderização baseada em shapes para máxima eficiência) e uma experiência de usuário agradável com estética 8-bits!

---

## ✨ Funcionalidades (Features)

* 🍎 **Mecânica Clássica:** Coma as maçãs para crescer e ganhar pontos. Cuidado para não morder o próprio rabo!
* 🌀 **Screen Wrap (Atravessar Bordas):** O jogo permite atravessar as paredes e sair do outro lado, tornando o gameplay mais fluido e dinâmico.
* 🚀 **Sistema de Boost (Combos):** Ao comer 5 maçãs normais, as próximas 3 maçãs se tornam maçãs douradas de boost que valem mais pontos (150 pontos em vez de 100!).
* ✨ **Animações de Feedback:** Textos flutuantes animados indicam quantos pontos você ganhou ao comer uma maçã (+100 ou +150).
* 🏆 **Sistema de Recorde:** O jogo salva automaticamente o seu "High Score" usando o sistema de Preferências, para que você não perca seu recorde ao fechar o jogo.
* 🎮 **Telas Polidas:** Tela inicial de boas-vindas e tela de Game Over interativas, utilizando fontes retrô autênticas (`PressStart2P`).

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java (Orientação a Objetos)
* **Framework:** [LibGDX](https://libgdx.com/) (Cross-platform game development)
* **Ferramentas de Build:** Gradle

---

## 🚀 Como Jogar (Tutorial)

### Opção 1: Jogar Imediatamente (Mais Fácil! ✨)
Se você quer apenas jogar sem precisar configurar um ambiente de desenvolvimento:
1. Acesse a aba de **[Releases](https://github.com/angellsim/snakeGame/releases)** aqui no repositório do GitHub.
2. Baixe o arquivo `SnakeGame-Windows.zip`.
3. Extraia o arquivo zip (clique com o botão direito -> Extrair Tudo).
4. Entre na pasta extraída e dê 2 cliques no arquivo `SnakeGame.exe`. O jogo vai abrir magicamente, sem precisar instalar nada!

### Opção 2: Compilar o Código (Para Desenvolvedores 💻)
Se você quiser rodar pelo código fonte:

**Pré-requisitos:**
1. **Java Development Kit (JDK)** instalado (versão 8 ou superior).
2. Uma IDE como **IntelliJ IDEA**, **Eclipse** ou **VS Code** com suporte a Java.

**Passos:**
1. **Clone o repositório:**
   ```bash
   git clone https://github.com/angellsim/snakeGame.git
   cd JogoDaCobrinha
   ```
2. **Abra na sua IDE:**
   - Importe o projeto como um **Projeto Gradle**. A IDE cuidará de baixar o LibGDX e todas as dependências necessárias automaticamente.
3. **Execute:**
   - Navegue até a classe `Lwjgl3Launcher.java` localizada na pasta `lwjgl3/src/main/java/io/angellsim/snakegame/lwjgl3/`.
   - Clique com o botão direito na classe e selecione **Run** (ou executar).
   - *Alternativa por linha de comando:*
     ```bash
     ./gradlew lwjgl3:run
     ```

---

## 🕹️ Controles do Jogo

| Ação | Tecla |
| :--- | :--- |
| **Iniciar / Reiniciar Jogo** | `ESPAÇO` |
| **Mover para Cima** | `Seta para Cima (UP)` |
| **Mover para Baixo** | `Seta para Baixo (DOWN)` |
| **Mover para Esquerda** | `Seta para Esquerda (LEFT)` |
| **Mover para Direita** | `Seta para Direita (RIGHT)` |

---

## 📚 Referências e Aprendizados

Durante o desenvolvimento deste jogo, foram explorados diversos conceitos importantes da Engenharia de Software e Desenvolvimento de Jogos:
- **Game Loop:** A arquitetura do método `render()` que atualiza a lógica do jogo e redesenha os gráficos na tela várias vezes por segundo.
- **Delta Time (`Gdx.graphics.getDeltaTime()`):** Movimentação e animações independentes de taxa de quadros (FPS).
- **Gerenciamento de Memória (`dispose()`):** A importância de liberar texturas, geradores (`FreeTypeFontGenerator`) e renderizadores para evitar *Memory Leaks*.
- **Estruturas de Dados:** Uso estratégico da `LinkedList` em Java para simular o corpo da cobra, inserindo a cabeça na frente e removendo o último segmento ao andar.

---
Feito com ☕ e muito código por **[Alice (Angellsim)](https://github.com/angellsim)** e **[Hugo Santos (Guguim)](https://github.com/guguim)**!
