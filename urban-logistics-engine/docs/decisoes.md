# Registo de decisões / Design decisions

[Português](#português) | [English](#english)

---

## Português

### 1. Prioridade das encomendas

**Problema.** A fila precisa de um número que indique qual encomenda atender primeiro.
**Alternativas.** Só urgência (ignora o valor do serviço); só taxa (quem paga pouco nunca é servido); acrescentar o tempo de espera (evita starvation).
**Escolha.** `prioridade = 20·urgência + 2·taxa + 0,1·(tempoAtual − chegada)`. Um nível de urgência vale 10 € ou 200 minutos de espera. Não se normalizam os fatores para a fórmula ficar interpretável.
**Chave fixa.** O termo `0,1·tempoAtual` é igual para todas as encomendas, por isso a ordem relativa não muda com o tempo. O heap ordena por `pontuacaoBase = 20·urgência + 2·taxa − 0,1·chegada`, calculada uma vez. Se a dependência do tempo fosse diferente por encomenda, o invariante do heap perdia-se.
**Complexidade.** O(1) em tempo e espaço.
**Limitações.** Os pesos são arbitrários e a espera não tem teto.

### 2. Fila de prioridades: heap binário

**Problema.** Atender sempre a encomenda de maior prioridade, com muitas inserções e remoções.
**Alternativas.** Lista não ordenada (extrair o máximo Θ(n)); lista ordenada (inserir O(n)); árvore balanceada (O(log n), mas com ponteiros e mais complexa).
**Escolha.** Heap binário em array: inserir e extrair em O(log n), máximo em Θ(1), sem ponteiros (filhos de i em 2i+1 e 2i+2).
**Como funciona.** Inserir coloca no fim e "sobe" enquanto for maior que o pai. Remover troca a raiz pelo último elemento, encolhe e "desce" o novo topo para o maior filho. O comparador define Max-Heap ou Min-Heap (reutilizado no Dijkstra).
**Complexidade.** inserir O(log n) amortizado, Ω(1); remover O(log n), Ω(1); espreitar Θ(1); remover(elemento) O(n); espaço Θ(n).
**Limitações.** Não pesquisa por identificador: cancelar custa O(n). Com um mapa de posições passaria a O(log n).

### 3. Lista duplamente ligada

**Problema.** Guardar o histórico e navegar nos dois sentidos.
**Alternativas.** `ArrayList` (remover a meio O(n)); lista simples (não recua).
**Escolha.** Lista duplamente ligada com referência à cabeça e à cauda: inserir/remover nas pontas e remover um nó conhecido em Θ(1).
**Complexidade.** Pontas e `remover(No)` Θ(1); `truncarApos` Θ(k+1); percorrer Θ(n); espaço Θ(n), com duas referências por nó.
**Limitações.** Sem acesso por índice em O(1); mais memória por elemento.

### 4. Histórico, navegação e Undo/Redo

**Problema.** Registar entrada, atribuição e cancelamento, navegar e desfazer/refazer atribuições.
**Escolha.** Duas listas: o registo completo (só cresce, com cursor de navegação) e a linha do tempo das atribuições, com um cursor `ultimaAplicada`. Desfazer recua o cursor, refazer avança-o, ambos em Θ(1). Uma atribuição ou um cancelamento descartam as atribuições desfeitas.
**Porquê duas listas.** O cursor do Undo não tem de saltar operações de outro tipo.
**Separação.** O histórico só devolve a operação; o `GestorEncomendas` aplica o efeito.
**Limitações.** Só as atribuições são desfazíveis; desfazer não fica no registo completo.

### 5. GestorEncomendas

**Problema.** Juntar fila, veículos, janelas e histórico sem misturar responsabilidades.
**Escolha.** O gestor é o único que altera o estado. O heap ordena, o histórico regista e o veículo controla a capacidade. Um mapa por identificador dá pesquisa em O(1) e deteta duplicados.
**Regras.** Só se cancelam encomendas pendentes (uma atribuída desfaz-se com Undo). Atribuir e cancelar invalidam o Redo. Os identificadores nunca se reutilizam.
**Validação antes de alterar.** A `Operacao` é criada e os limites verificados antes de mexer no estado, por isso uma falha não o deixa a meio.
**Complexidade.** receber O(log n); atribuirProxima O(log n); atribuir, cancelar e refazer O(n); desfazer O(log n); listarPendentes O(n log n).

### 6. Seleção de encomendas: greedy

**Problema.** Escolher as encomendas a carregar num veículo com limite de peso e de volume. É uma mochila com duas restrições, NP-difícil.
**Alternativas.** Força bruta O(2ⁿ); programação dinâmica; greedy.
**Escolha.** Greedy por densidade: valor = prioridade no instante; custo = `max(peso/pesoLivre, volume/volumeLivre)`; ordena por valor/custo decrescente e aceita o que ainda cabe.
**Porquê o `max`.** Com duas restrições, uma encomenda leve mas volumosa pode esgotar o volume; o `max` mede o recurso que mais limita.
**Complexidade.** O(n log n) em tempo, O(n) em espaço.
**Limitações.** É uma heurística. Capacidade 100 kg: A (60 kg, valor 70), B e C (50 kg, valor 50 cada). O greedy escolhe A e fica com 70; o ótimo é B+C, com 100.

### 7. Seleção de encomendas: knapsack

**Problema.** Obter a solução ótima onde o greedy falha.
**Escolha.** Mochila 0/1 com duas restrições por programação dinâmica: `melhor[p][v]` = maior valor com p unidades de peso e v de volume. Para cada encomenda percorre-se a tabela do fim para o início, para cada uma entrar no máximo uma vez.
**Discretização.** A capacidade livre de cada dimensão divide-se em R = 100 unidades e os tamanhos arredondam-se por excesso, por isso a solução nunca excede a capacidade real. A solução é ótima para o problema discretizado e pode perder algum valor em relação ao real.
**Complexidade.** Tempo O(n·R²); espaço O(R²) para os valores e O(n·R²) para reconstruir a escolha. Pseudo-polinomial.
**Greedy ou knapsack.** Greedy quando há muitas encomendas e o tempo conta; knapsack quando a qualidade da carga compensa o custo.

### 8. Seleção de intervalos

**Problema.** Escolher o maior número de janelas de entrega sem sobreposição.
**Alternativas.** Força bruta O(2ⁿ); programação dinâmica O(n²); greedy O(n log n).
**Escolha.** Ordenar pelo fim e aceitar a primeira janela que começa quando (ou depois de) a última aceite termina. Intervalos semiabertos [início, fim).
**Porque funciona.** Argumento de troca: numa solução ótima, trocar a primeira janela pela que termina mais cedo não reduz o número de janelas, porque deixa mais tempo livre.
**Porque não ordenar pelo início.** Uma janela longa que começa cedo bloquearia várias curtas.
**Complexidade.** Θ(n log n) em tempo, O(n) em espaço.
**Limitações.** Maximiza o número de entregas, não o valor.

### 9. Persistência

**Escolha.** Ficheiro de texto com um registo por linha (`CLOCK`, `VEHICLE`, `ORDER`, `WINDOW`) e campos separados por `;`. Guarda-se uma fotografia do estado e não o histórico, porque o registo inclui atribuições desfeitas. Ao carregar, o estado reconstrói-se pela API do gestor, que reaproveita as validações.
**Segurança.** Escreve-se num ficheiro temporário que depois substitui o original; o carregamento só substitui o estado atual se o ficheiro for lido por inteiro.
**Limitações.** O histórico e as encomendas canceladas não são guardados; carregar custa O(n²) no pior caso.

---

## English

### 1. Order priority

**Problem.** The queue needs a number that says which order to serve first.
**Alternatives.** Urgency only (ignores the value of the service); fee only (low payers are never served); adding waiting time (prevents starvation).
**Choice.** `priority = 20·urgency + 2·fee + 0.1·(currentTime − arrival)`. One urgency level equals 10 € or 200 minutes of waiting. Factors are not normalized, to keep the formula interpretable.
**Fixed key.** The term `0.1·currentTime` is the same for every order, so the relative order never changes over time. The heap sorts by `baseScore = 20·urgency + 2·fee − 0.1·arrival`, computed once. If the time dependence differed per order, the heap invariant would break.
**Complexity.** O(1) time and space.
**Limitations.** The weights are arbitrary and waiting time has no cap.

### 2. Priority queue: binary heap

**Problem.** Always serve the highest-priority order, with many insertions and removals.
**Alternatives.** Unsorted list (extract max Θ(n)); sorted list (insert O(n)); balanced tree (O(log n), but pointer-based and more complex).
**Choice.** Array-based binary heap: insert and extract in O(log n), max in Θ(1), no pointers (children of i at 2i+1 and 2i+2).
**How it works.** Insert places the element at the end and sifts up while it is larger than its parent. Remove swaps the root with the last element, shrinks, and sifts the new top down to the larger child. The comparator defines Max-Heap or Min-Heap (reused in Dijkstra).
**Complexity.** insert O(log n) amortized, Ω(1); remove O(log n), Ω(1); peek Θ(1); remove(element) O(n); space Θ(n).
**Limitations.** No lookup by id: cancelling costs O(n). A position map would make it O(log n).

### 3. Doubly linked list

**Problem.** Store the history and navigate both ways.
**Alternatives.** `ArrayList` (removing in the middle O(n)); singly linked list (cannot go back).
**Choice.** Doubly linked list with head and tail references: insert/remove at the ends and remove a known node in Θ(1).
**Complexity.** Ends and `remove(Node)` Θ(1); `truncateAfter` Θ(k+1); traversal Θ(n); space Θ(n), with two references per node.
**Limitations.** No O(1) index access; more memory per element.

### 4. History, navigation and Undo/Redo

**Problem.** Record entry, assignment and cancellation, navigate, and undo/redo assignments.
**Choice.** Two lists: the full log (grows only, with a navigation cursor) and the assignment timeline, with a cursor `lastApplied`. Undo moves the cursor back, redo moves it forward, both in Θ(1). An assignment or a cancellation discards the undone assignments.
**Why two lists.** The undo cursor never has to skip other operation types.
**Separation.** The history only returns the operation; the order manager applies the effect.
**Limitations.** Only assignments can be undone; undo is not recorded in the full log.

### 5. Order manager

**Problem.** Combine the queue, vehicles, windows and history without mixing responsibilities.
**Choice.** The manager is the only component that changes state. The heap orders, the history records and the vehicle tracks capacity. A map by id gives O(1) lookup and detects duplicates.
**Rules.** Only pending orders can be cancelled (an assigned one is reverted with Undo). Assigning and cancelling invalidate Redo. Ids are never reused.
**Validate before mutating.** The `Operacao` is built and limits are checked before touching state, so a failure never leaves it half-changed.
**Complexity.** receive O(log n); assign next O(log n); assign, cancel and redo O(n); undo O(log n); list pending O(n log n).

### 6. Order selection: greedy

**Problem.** Choose the orders to load into a vehicle with weight and volume limits. It is a two-constraint knapsack, NP-hard.
**Alternatives.** Brute force O(2ⁿ); dynamic programming; greedy.
**Choice.** Greedy by density: value = priority at that time; cost = `max(weight/freeWeight, volume/freeVolume)`; sort by value/cost descending and accept whatever still fits.
**Why the `max`.** With two constraints, a light but bulky order can exhaust the volume; `max` measures the limiting resource.
**Complexity.** O(n log n) time, O(n) space.
**Limitations.** It is a heuristic. Capacity 100 kg: A (60 kg, value 70), B and C (50 kg, value 50 each). Greedy picks A for 70; the optimum is B+C for 100.

### 7. Order selection: knapsack

**Problem.** Get the optimal solution where greedy fails.
**Choice.** 0/1 knapsack with two constraints by dynamic programming: `best[p][v]` = highest value with p weight units and v volume units. For each order the table is traversed from the end to the start, so each order is used at most once.
**Discretization.** The free capacity of each dimension is split into R = 100 units and sizes are rounded up, so the solution never exceeds the real capacity. It is optimal for the discretized problem and may lose some value compared with the real one.
**Complexity.** Time O(n·R²); space O(R²) for values and O(n·R²) to reconstruct the choice. Pseudo-polynomial.
**Greedy or knapsack.** Greedy when there are many orders and time matters; knapsack when load quality justifies the cost.

### 8. Interval scheduling

**Problem.** Choose the largest number of non-overlapping delivery windows.
**Alternatives.** Brute force O(2ⁿ); dynamic programming O(n²); greedy O(n log n).
**Choice.** Sort by end time and accept the first window that starts at or after the end of the last accepted one. Half-open intervals [start, end).
**Why it works.** Exchange argument: in an optimal solution, swapping the first window for the one that ends earliest does not reduce the count, because it leaves more free time.
**Why not sort by start.** A long window that starts early would block several short ones.
**Complexity.** Θ(n log n) time, O(n) space.
**Limitations.** It maximizes the number of deliveries, not their value.

### 9. Persistence

**Choice.** Text file with one record per line (`CLOCK`, `VEHICLE`, `ORDER`, `WINDOW`) and `;`-separated fields. A snapshot of the state is saved, not the history, because the log contains undone assignments. On load, the state is rebuilt through the manager's API, which reuses all validations.
**Safety.** It writes a temporary file that then replaces the original; loading only replaces the current state if the whole file was read.
**Limitations.** History and cancelled orders are not saved; loading costs O(n²) in the worst case.