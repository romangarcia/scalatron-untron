# scalatron-untron

1) Mapa de interes para las 8 coordenadas mas cercanas, y en menor grado para las lejanas

2) Estrategias segun estado del bot + estado del mapa + mision actual:
 . ajustar la estrategia actual en cada momento (puede cambiar de decision -- dejar de perseguir un good-beast si tengo un good-plant al lado)

a) Modos (master)
  - Cazador: Salir a la caza de energia
  - Paranoico: Correr!
  - Claustrofobico: Escapar de un lugar cerrado, con mucha pared y pocas salidas
  - Explorador: Pasear, conocer el lugar

b) Modos (mini)
  - Cazador
    - Volviendo a base
  - Misil
    - Atacando
    - Defensivo
  - Scout

Funcion (master):
 - Si se encuentra en un estado actual, cargar ese estado y analizar si un estado mejor seria posible
 - Si no se encuentra un estado actual, buscar un estado posible a partir de un analisis sobre el mapa visible
    + muchos 'goods' en la zona, cazador
    + muchos 'bads' en la zona, paranoico
    + muchas 'walls' en la zona, claustro
    + de otro modo, explorar




