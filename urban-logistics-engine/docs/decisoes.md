# Registo de decisões

## 1. Prioridade das encomendas

**Problema.** A fila de prioridades precisa de um número que diga qual
encomenda deve ser atendida primeiro.

**Fatores considerados.** Urgência (1 a 5, definida pelo cliente), taxa paga
(€) e tempo de espera (minutos).
- Só urgência: ignora o valor do serviço.
- Só taxa: quem paga pouco nunca é servido.
- Com tempo de espera: evita a starvation de encomendas de baixa prioridade.

**Escolha.** Soma ponderada linear:
`prioridade = 20 * urgência + 2 * taxa + 0.1 * (tempoAtual - chegada)`
Os pesos funcionam como taxas de câmbio: 1 nível de urgência equivale a
10 € de taxa e a 200 minutos de espera. Não normalizo os fatores porque isto
mantém a fórmula interpretável.

**Calculada, não guardada.** O tempo de espera muda, por isso a prioridade
é calculada para um instante dado.

**Consequência para o heap.** O termo do tempo é linear e igual para todas
as encomendas: `0.1 * tempoAtual` soma o mesmo valor a todas, logo a ordem
relativa nunca muda. Basta ordenar o heap por uma chave fixa
(`pontuacaoBase`), calculada na inserção, e o invariante do heap mantém-se.
Se o termo fosse não linear ou tivesse um teto, o heap deixaria de ser válido.

**Limitações.** Os pesos são arbitrários e dependem do contexto; noutro
negócio seriam outros. A espera não tem teto, por isso uma encomenda
antiga acaba sempre por passar à frente das outras.

**Complexidade.** O(1) tempo e espaço.

## 2. Fila de prioridades: heap binário

**Problema.** Atender sempre a encomenda de maior prioridade, com muitas inserções e remoções.
**Alternativas.** Lista ordenada (inserção O(n)), lista não ordenada (remoção do máximo O(n)),
árvore balanceada (O(log n) mas com ponteiros e mais complexa).
**Escolha.** Heap binário em array: inserção e remoção do máximo em O(log n), máximo em O(1),
sem ponteiros (filhos de i em 2i+1 e 2i+2).
**Como funciona.** Inserir coloca no fim e "sobe" enquanto for maior que o pai. Remover troca a
raiz pelo último, encolhe e "desce" o novo topo para o maior filho.
**Comparador.** Raiz = maior segundo o comparador; Min-Heap inverte-o (reutilizado no Dijkstra).
**Complexidade.** inserir O(log n), Ω(1); remover O(log n); espreitar Θ(1);
remover(elemento) O(n) (pesquisa linear) + O(log n); espaço Θ(n).
**Limitações.** Não há pesquisa eficiente; cancelar uma encomenda arbitrária custa O(n).

## 3. Lista duplamente ligada

**Problema.** Guardar o histórico e navegar nos dois sentidos.
**Alternativas.** ArrayList (remoção a meio O(n)), lista simples (não recua).
**Escolha.** Lista duplamente ligada com referências à cabeça e à cauda: inserir/remover nas
pontas e remover um nó conhecido em Θ(1); recuar e avançar em Θ(1).
**Complexidade.** adicionar/remover nas pontas Θ(1); remover(nó) Θ(1); truncarApos O(k);
pesquisa O(n), Ω(1); espaço Θ(n) (2 referências por nó).
**Limitações.** Sem acesso por índice em O(1); mais memória por elemento.

## 4. Histórico, navegação e Undo/Redo

**Problema.** Registar entrada, atribuição e cancelamento, navegar e desfazer/refazer atribuições.
**Escolha.** Duas listas: o registo completo (só cresce) e a linha do tempo das atribuições com
um cursor. Desfazer recua o cursor, refazer avança-o, ambos em Θ(1). Uma nova atribuição
descarta as desfeitas (truncarApos).
**Separação.** O histórico só devolve a operação; quem aplica o efeito é o GestorEncomendas.
**Limitações.** Só as atribuições são desfazíveis e o cancelamento invalida o refazer; desfazer não fica registado no registo.

## 5. GestorEncomendas

**Problema.** Juntar fila, veículos e histórico sem misturar responsabilidades.
**Escolha.** O gestor é o único que altera o estado. O heap só ordena, o histórico só
regista e devolve a operação a desfazer/refazer, e o veículo só controla a capacidade.
**Pendentes.** Um HashMap por id dá pesquisa em O(1) e deteta duplicados; o heap não
oferece pesquisa por id (O(n)).
**Undo/Redo.** Desfazer descarrega o veículo e reinsere a encomenda no heap. A ordem
mantém-se porque a chave (pontuacaoBase) não depende do instante.
**Regras.** Só se cancelam encomendas pendentes (uma atribuída desfaz-se com Undo).
Atribuir e cancelar invalidam o Redo. Os identificadores nunca se reutilizam.
**Validação antes de alterar.** A Operacao é criada e os limites verificados antes de
mexer no heap, por isso uma falha não deixa o sistema a meio.
**Complexidade.** receber O(log n); atribuirProxima O(log n); atribuir/cancelar/refazer
O(n) (remoção de um elemento qualquer no heap); desfazer O(log n); listarPendentes O(n log n).
**Limitações.** Remover um elemento arbitrário do heap é O(n); com um mapa de posições
passaria a O(log n), à custa de mais complexidade.

## 6. Seleção de encomendas por veículo: estratégia gulosa

**Problema.** Escolher que encomendas pendentes carregar num veículo com limite de peso
e de volume, privilegiando as mais prioritárias. É uma mochila com duas restrições (NP-difícil).
**Alternativas.** Força bruta (O(2^n)), programação dinâmica (precisaria de discretizar
peso e volume, com muito espaço), greedy.
**Escolha.** Greedy por densidade: valor = prioridade no instante; custo = fração da capacidade
livre que a encomenda consome no recurso mais escasso, max(peso/pesoLivre, volume/volumeLivre).
Ordena-se por valor/custo decrescente e aceita-se o que ainda cabe.
**Porquê.** Usa os dois limites numa só medida, escala bem e é fácil de explicar.
**Como funciona.** 1) descarta as que nem sozinhas cabem; 2) calcula a densidade; 3) ordena;
4) percorre uma vez e aceita se cabe em peso e volume.
**Complexidade.** O(n log n) tempo (a ordenação domina), O(n) espaço.
**Limitações.** Não é ótimo. Capacidade 10 kg: A (6 kg, valor 7), B e C (5 kg, valor 5.5).
O greedy escolhe A (7) e depois nenhuma outra cabe, mas B+C valem 11.
Serve quando o tempo de cálculo conta mais do que o ótimo exato.