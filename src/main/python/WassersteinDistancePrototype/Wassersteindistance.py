# -*- coding: utf-8 -*-
"""
Created on Wed May  8 16:08:12 2019

@author: Styx
"""

import numpy as np
import random
import matplotlib.pyplot as plt
import math
import networkx as nx

def show_graph(adjacency_matrix):
    rows, cols = np.where(adjacency_matrix != 0)
    edges = zip(rows.tolist(), cols.tolist())
    gr = nx.DiGraph()
    gr.add_edges_from(edges)
    nx.draw(gr, node_size=500)
    plt.show()

#actual only important part
def st_flow(u,c,s,t):
    rows, cols = np.where(u != 0)
    edges = zip(rows.tolist(), cols.tolist())
    
    G = nx.DiGraph()
    G.add_edges_from(edges)
    
    #demands = {0: {'demand': -20}, 1: {'demand': 20}}
    #nx.set_node_attributes(G, demands)
    #print(G.nodes[0]['demand'])
    
    edgeattributes = {(0,2) : {'capacity':u[0][2]}}
    for e in G.edges:
        if not (e[0] == 0 and e[1] == 2):
            edgeattributes[e] = {'capacity': u[e[0]][e[1]]}
    
    for i in range(n):
        for j in range(m):
            edgeattributes[(3+i,3+n+j)]["weight"] = int(c[3+i][3+n+j]*100)
#h->Y, Y->t
    for j in range(m):
        edgeattributes[(2,3+n+j)]["weight"] = int(c[2][3+n+j]*100)
    
    nx.set_edge_attributes(G,edgeattributes)
    #print(edgeattributes)
    
    maxFlow = nx.max_flow_min_cost(G, s, t)
    
    #print(maxFlow)
    print(nx.cost_of_flow(G,maxFlow)/100)
    
    mapping = []
    
    for i in range(n):
        edges = maxFlow[3+i]
        #print("x_"+str(i) + " -> y_" + str(max(edges, key=(lambda k, mapping=edges: mapping[k] and k))-3-n))
        mapping.append(max(edges, key=(lambda k, mapping=edges: mapping[k] and k))-3-n)

    
    #nx.draw(G, node_size=500)
    #plt.show()
    
    return mapping

n = 30
m = 40

X = []
Y = []
epsilon = 30

#completely irrelevant point generation
for i in range(n):
    x = random.randint(0,500)
    y = random.randint(x,500)
    X.append([x,y])
for i in range(m):
    x = 0
    y = 0
    if i < n:
        x = random.randint(max(X[i][0]-epsilon,0),min(X[i][0]+epsilon,500))
        y = random.randint(max(X[i][1]-epsilon,x),min(X[i][1]+epsilon,500))
    else:
        x = random.randint(0,500)
        y = random.randint(x,min(500,x+50))
    Y.append([x,y])
X = np.array(X)
Y = np.array(Y)

plt.scatter(X[:,0],X[:,1],color="r")
plt.scatter(Y[:,0],Y[:,1],color="b")
plt.plot([0,500],[0,500])
plt.show()

#capacity and cost calculations:
cap = np.zeros((3+n+m,3+n+m))
cost = np.zeros((3+n+m,3+n+m))



#s = 0, t = 1, h = 2

#s->x
#0 cost
for i in range(n):
    cap[0][3+i] = 1
cap[0][2] = m-n

#X->Y
for i in range(n):
    for j in range(m):
        cap[3+i][3+n+j] = 1
        cost[3+i][3+n+j] = math.sqrt((X[i][0]-Y[j][0])**2 + (X[i][1]-Y[j][1])**2)
#h->Y, Y->t
for j in range(m):
    cap[2][3+n+j] = 1
    cost[2][3+n+j] = (Y[j][1] - Y[j][0])/math.sqrt(2)
    cap[3+n+j][1] = 1

np.set_printoptions(precision=1)
#print(cap)
#print(cost)

mapping = st_flow(cap,cost,0,1)
print(mapping)

plt.figure(figsize=(12,12))
plt.scatter(X[:,0],X[:,1],color="r")
plt.scatter(Y[:,0],Y[:,1],color="b")
plt.plot([0,500],[0,500])
for i in range(len(mapping)):
    plt.plot([X[i][0],Y[mapping[i]][0]],[X[i][1],Y[mapping[i]][1]],color="g")
for i in range(m):
    if i not in mapping:
        diff = Y[i][1] - Y[i][0]
        plt.plot([Y[i][0],Y[i][0]+diff/2],[Y[i][1],Y[i][0]+diff/2],color="g")
        #pass
        #plt.plot([Y[i][0],Y[i][0]],[Y[i][0],Y[i][1]],color="g")
plt.show()
#show_graph(cap)

