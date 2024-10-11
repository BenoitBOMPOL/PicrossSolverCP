#!/usr/bin/env python3
import json
import graphviz
from itertools import combinations
from sys import argv

def create_graph(base_name: str):
    # Génération des combinaisons de contraintes
    constraint_combinations = sum([[''.join(sk_) for sk_ in combinations('ABCDEF', k_)] for k_ in range(1, 7)], [])
    
    # Liste des sommets
    vertices = []
    for cc_ in constraint_combinations:
        # Charger les données du fichier JSON correspondant
        with open(f'outputs/{base_name}_{cc_}.json', 'r') as cc_output:
            cc_data = json.load(cc_output)
            if cc_data['is_valid']:
                vertices.append(cc_)
    
    # Génération des arcs
    arcs = [(u_, v_) for u_ in vertices for v_ in vertices if (len(v_) == len(u_) + 1) and all(wu_ in v_ for wu_ in u_)]
    
    # Points d'entrée (sommets sans prédécesseurs)
    entry_points = set([u_ for (u_, _) in arcs if u_ not in [w_ for (__, w_) in arcs]])
    
    # Création du graphe avec graphviz
    dot = graphviz.Digraph(comment='Setup of constraints')
    for v_ in vertices:
        dot.node(v_)
    
    for (u_, v_) in arcs:
        dot.edge(u_, v_)
    
    # Générer et sauvegarder le graphe
    dot.render(f'graph_outputs/graphrender_{base_name}', format='pdf', cleanup=True)

if __name__ == '__main__':
    create_graph(argv[1])
