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