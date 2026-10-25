# Urban Logistics Engine

[Português](#português) | [English](#english)

---

## Português

Motor de simulação e otimização logística urbana em Java, desenvolvido para a UC de Algoritmos e Estruturas de Dados (Fase 1). O foco está nas estruturas de dados e nos algoritmos, e na justificação de cada escolha.

### O que inclui

- **Fila de prioridades** de encomendas com um heap binário genérico (Max-Heap, com `Comparator`)
- **Histórico de operações** numa lista duplamente ligada, com navegação e Undo/Redo de atribuições
- **Empacotamento** de encomendas num veículo (limite de peso e de volume) com greedy e knapsack (programação dinâmica)
- **Seleção de intervalos** para otimizar janelas de entrega
- **Persistência** do estado em ficheiro de texto

### Requisitos

- JDK 11 ou superior
- Maven (opcional; o projeto é um projeto Maven do NetBeans)

### Compilar e executar

**NetBeans:** File > Open Project, escolher a pasta do projeto e Run (classe principal: `urbanlogistics.UrbanLogisticsEngine`).

**Linha de comandos** (a partir da raiz do projeto, onde está o `pom.xml`):

```
mvn compile
java -cp target/classes urbanlogistics.UrbanLogisticsEngine
```

Sem Maven:

```
javac -d out $(find src/main/java -name "*.java")     # Linux/macOS
java -cp out urbanlogistics.UrbanLogisticsEngine
```

No Windows: `dir /s /b src\main\java\*.java > sources.txt` e depois `javac -d out @sources.txt`.

> Executar sempre a partir da raiz do projeto: o estado é guardado em `data/state.txt` (caminho relativo).

### Como usar

| Opção | Ação |
|---|---|
| 1 / 14 | Registar / listar veículos |
| 2 / 3 | Receber encomenda / listar pendentes por prioridade |
| 4 | Atribuir a encomenda mais prioritária a um veículo |
| 5 / 6 | Carregar um veículo com greedy / com knapsack |
| 7 | Comparar o que cada estratégia escolheria (sem alterar nada) |
| 8 | Cancelar uma encomenda pendente |
| 9 / 10 | Desfazer / refazer atribuição |
| 11 / 12 | Adicionar janela de entrega / calcular o maior conjunto sem sobreposição |
| 13 | Navegar no histórico |
| 15 | Avançar o relógio da simulação |
| 16 | Carregar estado de um ficheiro |
| 0 / 17 | Guardar e sair / sair sem guardar |

### Exemplo rápido

1. Executar o programa e escolher a opção **16** com `data/example.txt`.
2. Opção **7** com o veículo `V1`: o greedy escolhe `O1` (valor 70); o knapsack escolhe `O2` e `O3` (valor 100). É o caso em que o greedy não é ótimo.
3. Opção **12**: seleciona as janelas `W1` e `W3` (2 de 4, sem sobreposição).

### Formato do ficheiro de estado

Um registo por linha, campos separados por `;`. Linhas que começam por `#` são comentários.

```
CLOCK;<minutos>
VEHICLE;<id>;<capacidade peso kg>;<capacidade volume m3>
ORDER;<id>;<peso>;<volume>;<chegada>;<taxa>;<urgência 1-5>;<PENDING|ASSIGNED>;<id veículo ou vazio>
WINDOW;<id>;<id encomenda>;<início>;<fim>
```

### Estrutura do projeto

```
src/main/java/urbanlogistics/
├── UrbanLogisticsEngine.java   ponto de entrada
├── model/         Encomenda, Veiculo, Operacao, TipoOperacao, DeliveryWindow
├── structures/    BinaryHeap, DoublyLinkedList
├── algorithms/    GreedyPacking, KnapsackPacking, PackingStrategy, IntervalScheduling
├── service/       GestorEncomendas, HistoricoOperacoes
├── persistence/   StateStorage
└── ui/            MainMenu
data/              ficheiros de exemplo
docs/              decisões de desenho, relatório, uso de IA
```

### Documentação

- `docs/decisoes.md`: para cada estrutura e algoritmo, o problema, as alternativas, a escolha, como funciona, a complexidade e as limitações (PT e EN)
- `docs/uso-de-ia.md`: como foi usada a IA neste trabalho
- Relatório da Fase 1: ver `docs/`

### Complexidade (resumo)

| Componente | Complexidade |
|---|---|
| Heap: inserir / remover máximo / espreitar | O(log n) / O(log n) / Θ(1) |
| Lista duplamente ligada: inserir e remover nas pontas | Θ(1) |
| Histórico: recuar, avançar, desfazer, refazer | Θ(1) |
| Greedy | O(n log n) |
| Knapsack (R = resolução) | O(n · R²) |
| Seleção de intervalos | Θ(n log n) |

---

## English

Urban logistics simulation and optimization engine in Java, built for the Algorithms and Data Structures course (Phase 1). The focus is on the data structures and algorithms, and on justifying each choice.

### Features

- **Priority queue** of orders using a generic binary heap (Max-Heap, with a `Comparator`)
- **Operation history** in a doubly linked list, with navigation and Undo/Redo of assignments
- **Packing** orders into a vehicle (weight and volume limits) with a greedy strategy and a knapsack (dynamic programming)
- **Interval scheduling** to optimize delivery windows
- **Persistence** of the state in a text file

### Requirements

- JDK 11 or higher
- Maven (optional; this is a NetBeans Maven project)

### Build and run

**NetBeans:** File > Open Project, choose the project folder and Run (main class: `urbanlogistics.UrbanLogisticsEngine`).

**Command line** (from the project root, where `pom.xml` is):

```
mvn compile
java -cp target/classes urbanlogistics.UrbanLogisticsEngine
```

Without Maven:

```
javac -d out $(find src/main/java -name "*.java")     # Linux/macOS
java -cp out urbanlogistics.UrbanLogisticsEngine
```

On Windows: `dir /s /b src\main\java\*.java > sources.txt`, then `javac -d out @sources.txt`.

> Always run from the project root: the state is saved to `data/state.txt` (relative path).

### Usage

| Option | Action |
|---|---|
| 1 / 14 | Register / list vehicles |
| 2 / 3 | Receive an order / list pending orders by priority |
| 4 | Assign the highest-priority order to a vehicle |
| 5 / 6 | Load a vehicle using greedy / knapsack |
| 7 | Compare what each strategy would pick (changes nothing) |
| 8 | Cancel a pending order |
| 9 / 10 | Undo / redo an assignment |
| 11 / 12 | Add a delivery window / compute the largest non-overlapping set |
| 13 | Browse the history |
| 15 | Advance the simulation clock |
| 16 | Load state from a file |
| 0 / 17 | Save and exit / exit without saving |

### Quick example

1. Run the program and choose option **16** with `data/example.txt`.
2. Option **7** with vehicle `V1`: greedy picks `O1` (value 70); knapsack picks `O2` and `O3` (value 100). This is a case where greedy is not optimal.
3. Option **12**: selects windows `W1` and `W3` (2 of 4, non-overlapping).

### State file format

One record per line, fields separated by `;`. Lines starting with `#` are comments.

```
CLOCK;<minutes>
VEHICLE;<id>;<weight capacity kg>;<volume capacity m3>
ORDER;<id>;<weight>;<volume>;<arrival>;<fee>;<urgency 1-5>;<PENDING|ASSIGNED>;<vehicle id or empty>
WINDOW;<id>;<order id>;<start>;<end>
```

### Project structure

```
src/main/java/urbanlogistics/
├── UrbanLogisticsEngine.java   entry point
├── model/         Encomenda, Veiculo, Operacao, TipoOperacao, DeliveryWindow
├── structures/    BinaryHeap, DoublyLinkedList
├── algorithms/    GreedyPacking, KnapsackPacking, PackingStrategy, IntervalScheduling
├── service/       GestorEncomendas, HistoricoOperacoes
├── persistence/   StateStorage
└── ui/            MainMenu
data/              sample files
docs/              design decisions, report, AI usage
```

### Documentation

- `docs/decisoes.md`: for each data structure and algorithm, the problem, alternatives, choice, how it works, complexity and limitations (PT and EN)
- `docs/uso-de-ia.md`: how AI was used in this project
- Phase 1 report: see `docs/`

### Complexity (summary)

| Component | Complexity |
|---|---|
| Heap: insert / remove max / peek | O(log n) / O(log n) / Θ(1) |
| Doubly linked list: insert and remove at the ends | Θ(1) |
| History: back, forward, undo, redo | Θ(1) |
| Greedy | O(n log n) |
| Knapsack (R = resolution) | O(n · R²) |
| Interval scheduling | Θ(n log n) |
