#!/usr/bin/python3
from tkinter import *
from tkinter.filedialog import askopenfilename
from PIL import Image,ImageTk
from scipy.spatial import Voronoi
from scipy.sparse import csr_matrix as sparse
import numpy as np
import networkx as nx
import matplotlib.pyplot as plt
import pydot
from IPython.display import display
from IPython.display import Image as Img
from io import StringIO
from scipy.linalg import null_space
import time


points = []#[[100,100],[100,200],[200,100],[200,200]]
vorvertices = []

drawVoronoi = True

root = Tk()
File = askopenfilename(parent=root, initialdir="~/Downloads/",title='Choose an image.')
img = ImageTk.PhotoImage(Image.open(File))

canvas = Canvas(root, width=img.width(), height = img.height())
#canvas.pack(side="bottom", fill = "both", expand = "yes")
canvas.grid(row=0,column=0,sticky=N+W)
canvas.create_image(0,0,image = img, anchor = NW)
#panel.grid(row=0,column=0,sticky=N+W)

def _create_circle(self, x, y, r, **kwargs):
    return self.create_oval(x-r, y-r, x+r, y+r, **kwargs)
Canvas.create_circle = _create_circle

def _create_line(self, x, y, **kwargs):
    return self.create_line(x[0],x[1],y[0],y[1], **kwargs)
Canvas.create_pointline = _create_line

G = nx.Graph()

mothernodecount = 0
nodecount = 0#4

def twodet(a,b):
    return a[0]*b[1] - a[1]*b[0]

def detectInside(G,v):
    neighbors = G.neighbors(v)
    N = []
    for p in neighbors:
        N.append(p)
    if len(N) == 4:
        return True
    if len(N)!=3:
        print("bad")
        return False
    i = v - nodecount
    
    ver = np.array(vorvertices[i])
    a0 = np.array(points[N[0]])
    a1 = np.array(points[N[1]]) - a0
    a2 = np.array(points[N[2]]) - a0

#    print("v " + str(vorvertices[i]))
#    print("0 " + str(a0))
#    print("1 " + str(a1))
#    print("2 " + str(a2))
    nenner = twodet(a1,a2)
    if nenner == 0:
        return False
    a = (twodet(ver,a2) - twodet(a0,a2))/nenner
    b = (twodet(a0,a1) - twodet(ver,a1))/nenner
#    print(a)
#    print(b)
    if a>0:
        if b>0:
            if a+b<1:
                return True
    return False

def view_pydot_3(pdota,pdotb,pdotc):
    pdota.write_png("tempa.png")
    imga = Image.open("tempa.png")

    pdotb.write_png("tempb.png")
    imgb = Image.open("tempb.png")

    pdotc.write_png("tempc.png")
    imgc = Image.open("tempc.png")

    fig=plt.figure(figsize=(10, 10))

    fig.add_subplot(3,1,1)
    plt.imshow(imga)

    fig.add_subplot(3,1,2)
    plt.imshow(imgb)

    fig.add_subplot(3,1,3)
    plt.imshow(imgc)
    #imgplot = plt.imshow(img)
    plt.show()

def drawGraph(G,H,Q):
    #pos = nx.spring_layout(G)  # positions for all nodes

    # nodes
    #color_map = []
    #for node in G:
    #    if node <nodecount:
    #        color_map.append('blue')
    #    else: color_map.append('green')      
    #nx.draw_networkx_nodes(G,pos,node_color = color_map,with_labels = True)

    # edges
    #nx.draw_networkx_edges(G, pos, width=6)
    #labels = nx.get_edge_attributes(G,'weight')
    #nx.draw_networkx_edge_labels(G,pos,edge_labels=labels)

    # labels
    #nx.draw_networkx_labels(G, pos, font_size=20, font_family='sans-serif')
    #plt.axis('off')
    #plt.show()

    view_pydot_3(nx.nx_pydot.to_pydot(G),nx.nx_pydot.to_pydot(H),nx.nx_pydot.to_pydot(Q))


inside = []

def takeLast(e):
    return e[-1]

def removeEdgeFromMorseActions(A,e):
    for a in A:
        if a[0] == 'edge':
            if a[1][0] == e[0] and a[1][1] == e[1]:
                A.remove(a)
                return A
            if a[1][0] == e[1] and a[1][1] == e[0]:
                A.remove(a)
                return A
    return A

def calculateHom2(G):
    #TODO:
    #edges: add edges. detect cycles
    #faces: add relation
    #build matrix as follows:
    #[abcd|rst]
    #where a-d are cycles and r-t are relations
    #remove some cycle that is indicated in a vector in the nullspace
    #we dont know edge mappings anymore (?)
    t0 = time.time()
    #find (non)degenerate faces:
    global inside
    inside = []

    for i in range(mothernodecount):
        inside.append(detectInside(G,i+nodecount))
    
    #idea/concept:

    #step 1: create filtration    
    numberofnullspacecalcs = 0
    MorseActions = []
    for e in G.subgraph(range(nodecount)).edges(data='weight'):
        MorseActions.append(['edge',[e[0],e[1]],e[2]/2])
    
    for i in range(mothernodecount):
        if detectInside(G,i+nodecount):
            neighbors =  [u for u in G.neighbors(i+nodecount)]
            edges = [e for e in G.edges(i+nodecount,data='weight')]
            MorseActions.append(['face',neighbors,edges[0][2]])
        else:
            neighbors = [u for u in G.neighbors(i+nodecount)]
            edges = [e for e in G.subgraph(neighbors).edges(data='weight')]
            edges.sort(key=takeLast)
            MorseActions = removeEdgeFromMorseActions(MorseActions,[edges[2][0],edges[2][1]])
            #lowest edge is simply an edge:
            #MorseActions.append(['edge',[edges[0][0],edges[0][1]],edges[0][2]/2])
            #secondlowest edge is a degenerate Edge
            #MorseActions.append(['faceafteredge',[edges[1][0],edges[1][1]],neighbors,edges[1][2]/2])
            #highest edge is only adjusting the edgeMapping

            #TODO: fix handling of degenerate triples

            #2:32 idea:
            #longest edge gets contracted somehow, to one edge, instead of one vertex. when reaching the voronoi point of the triple, we may contract to point??
            MorseActions.append(['faceafteredge',[edges[2][0],edges[2][1]],neighbors,edges[2][2]/2])
            
    MorseActions.sort(key=takeLast)

    #print(MorseActions)

    #Morse graph to detect cycles
    H = nx.Graph()
    H.add_nodes_from(G.subgraph(range(0,nodecount)))

    #create storage for edge Mapping from H_t -> H_t^red, nodeMapping H_t->H_t^red, and connectedMap to store the connected components of H_t
    connectedMap = np.array([],dtype='int_')
    zerothHomology = []
    cycles = []
    relations = []
    firstHomology = []

    for i in range(nodecount):
        connectedMap = np.append(connectedMap,i)

        #[life,death]
        zerothHomology.append([0,float("inf")])

    edgeIdCounter = 0

    for action in MorseActions:
        #print("current action: " + str(action))
        faceafteredge = action[0] == 'faceafteredge'
        if action[0] == 'edge' or faceafteredge:
            #print(action[1])
            u = action[1][0]
            v = action[1][1]
            #detect connected components/cycles
            if connectedMap[u] != connectedMap[v]:
                if connectedMap[u] < connectedMap[v]:
                    zerothHomology[connectedMap[v]][1] = action[2]
                else:
                    zerothHomology[connectedMap[u]][1] = action[2]
                replacevalue = max(connectedMap[u],connectedMap[v])
                newvalue = min(connectedMap[u],connectedMap[v])

                for i in range(len(connectedMap)):
                    if connectedMap[i] == replacevalue:
                        connectedMap[i] = newvalue
                #print("connected Map:"+str(connectedMap))
                #print("first persisten Homology:"+str(firstHomology))

            elif connectedMap[u] == connectedMap[v]:#and not faceafteredge:
                p = [v for v in nx.shortest_path(H,source=u,target = v,weight='weight')]
                edgewalkIds = []
                for i in range(len(p)-1):
                    edgewalkIds.append([H.edges[p[i],p[i+1]]["Id"],p[i]>p[i+1]])
                edgewalkIds.append([edgeIdCounter,v>u])
                #[cycle,birth]
                cycles.append([edgewalkIds,action[-1]])
            
            #add new edge
            #might reach this statement with faceafteredge, and then action[2] is not the weight we want
            H.add_edge(u,v, Id = edgeIdCounter,weight=action[-1])
            edgeIdCounter += 1
        if faceafteredge:
            #bad hack
            action = ['face',action[2],action[3]]

        if action[0] == 'face':
            #we get a list S of nodes in the original graph G
            #this is a list S of edges in the graph H
            #S = action[1]
            #print("S:" + str(S))

            #this induces the to-contract face in H H[S]
            #HS = H.subgraph(S)

            #we get Edges E of H[S] and Vertices V of H[S]=S
            #E = HS.edges().data("Id")
            
            #these Edges have EdgeIds, and the Vertices have VertexIds
            #EdgeIds = [e[2] for e in E]
            #print("EdgeIds:" + str(EdgeIds))
            
            #we can map these to Q via edgeMapping and nodeMapping onto sets of Ids Eid and Vid
            #look at the uniques of these sets
            #QEdgeIds = np.unique(edgeMapping[np.array(EdgeIds)])
            #QVertexIds = np.sort(np.unique(nodeMapping[np.array(S)]))
            #QEdgeIds = QEdgeIds[QEdgeIds>=0]


            #print("QEdgeIds:" + str(QEdgeIds))
            #print("QVertexIds:" + str(QVertexIds))

            #find all Edges in Q with these Ids, E'
            #Ebar = []
            #for e in Q.subgraph(QVertexIds).edges(data="Id",keys=True):
            #    #e = (u,v,key,data)
            #    if e[3] in QEdgeIds:
            #        Ebar.append((e[0],e[1],e[2]))
            #        edgeMapping[e[3]]=-1
            #print("E':" + str(Ebar))
            #print("edgeMap:"+str(edgeMapping))

            #find all Vertices in Q with these Ids, V'
            #Vbar = QVertexIds

            #remove all edges E' from Q (dont forget adjusting the mapping)
            #Q.remove_edges_from(Ebar)

            #contract these vertices C' to some vertex (dont forget adjusting mapping)
            #for v in range(1,len(QVertexIds)):
            #    Q = nx.contracted_nodes(Q,QVertexIds[0],QVertexIds[v])
            #for i in range(len(nodeMapping)):
            #    if nodeMapping[i] in QVertexIds:
            #        nodeMapping[i] = QVertexIds[0]
            #print("nodeMap:"+str(nodeMapping))

            #first Homology calculation
            #map cycles through edgeMapping
            try:
                newrel = []
                for e in nx.find_cycle(H.subgraph(action[1])):
                    newrel.append([H.get_edge_data(*e)["Id"],e[0]<e[1]])
                relations.append(newrel)
            except nx.exception.NetworkXNoCycle:
                print("CycleError!!")
                print("  on action:"+str(action))
                print("  subgraphedges are:"+str(H.subgraph(action[1]).edges()))

            #quotientcycles = []
            #trivialdeaths = []
            #for i in range(len(cycles)):
            #    quotientcycles.append(edgeMapping[np.array([a[0] for a in cycles[i][0]])])

            #trivial deaths
            #for i in range(len(quotientcycles)):
            #    if np.max(quotientcycles[i])==-1:
            #        trivialdeaths.append(i)
            #print("cycles:"+str(cycles))
            #print("edgeMapping:"+str(edgeMapping))
            #print("quotientcycles:"+str(quotientcycles))
            #print("trivial deaths:"+str(trivialdeaths)+"\n")
            #for i in reversed(trivialdeaths):
            #    if cycles[i][1] != action[-1]:
            #        firstHomology.append([cycles[i][1],action[-1]])
            #    cycles.pop(i)
            #print("cycles after trivial deaths:"+str(cycles))
            #print("first homology after trivial deaths:" + str(firstHomology))

            #nontrivial deaths
            
            #print("indiceslist:"+str(indiceslist))
            #indices = {}
            #for i in range(len(indiceslist)):
            #    indices[indiceslist[i]] = i
            #print("indices:" + str(indices))

            #all thats left in cycles are nontrivial cycles
           
            #nontrivialdeaths = []
            #if len(cycles) != 0:
            #    matrix = np.zeros((len(indices),len(cycles)))
            #    for i in range(len(cycles)):
            #        for j in range(len(cycles[i])):
            #            if edgeMapping[cycles[i][0][j][0]]==-1:
            #                continue
            #            toadd = -1
            #            if cycles[i][0][j][1]:
            #                toadd = 1
            #            matrix[indices[edgeMapping[cycles[i][0][j][0]]],i] += toadd
            #    print("matrix:\n"+str(matrix))
            #    nullspace = null_space(matrix)
            #    print("nullspace:\n"+str(nullspace))
            #    for column in nullspace.T:
            #        nontrivialdeaths.append(np.max(np.nonzero(column)))
            #    print(nontrivialdeaths)
            #for i in reversed(nontrivialdeaths):
            #    if cycles[i][1] != action[-1]:
            #        firstHomology.append([cycles[i][1],action[-1]])
            #    cycles.pop(i)
            #print("cycles after nontrivial deaths:"+str(cycles))
            #print("first homology after nontrivial deaths:" + str(firstHomology))
            #if cycle consists of only -1s it is apriori zero and can be removed
            #generate a matrix, that has columns = cycle-vectors
            #find kernel of matrix
            

            done = False
            while not done:
                done = False
                matrix = np.zeros((edgeIdCounter,len(cycles)+len(relations)))
                for i in range(len(cycles)):
                    #print("i:"+str(i))
                    cycle = cycles[i][0]
                    for j in range(len(cycle)):
                        toadd = -1
                        if cycle[j][1]:
                            toadd = 1
                        matrix[cycle[j][0],i] += toadd
            #same for relations
                for i in range(len(relations)):
                    rel = relations[i]
                    for j in range(len(rel)):
                        toadd = -1
                        if rel[j][1]:
                            toadd = 1
                        matrix[rel[j][0],i+len(cycles)] += toadd
                #print(matrix)
                nullspace = null_space(matrix)
                numberofnullspacecalcs += 1
                if len(nullspace)!=0:
                    maxnonzero = -1
                    for column in nullspace.T:
                        for i in range(len(cycles)):
                            if column[i]!=0:
                                if i > maxnonzero:
                                    maxnonzero = i
                    if maxnonzero != -1:
                        done = True
                        if cycles[i][1] != action[-1]:
                            firstHomology.append([cycles[i][1],action[-1]])
                        cycles.pop(i)
                #print("firstHomology:"+str(firstHomology))

                #if contains nonzero entry in cyclerange, remove cycle from first Homology, recalculate
                #add killed cycle to firstHomology
                #else break
                

        #drawGraph(G,H,Q)
    #kill final first homology for nice picture:
    #print("connectedMap"+str(connectedMap))
    #print("changed"+str(changed))
    zerothHomology[0][1] = MorseActions[-1][-1]
    t1 = time.time()
    print("\n\n---  Homologies for the given instance  ---")
    print("   persistance diagram for the zeroth Homology:\n"+str(zerothHomology))
    print("\n   persistance diagram for the first Homology:\n"+str(firstHomology))

    #hacked visualization
    plt.scatter(*(np.array(zerothHomology).T),c='r')
    if len(firstHomology) != 0:
        plt.scatter(*(np.array(firstHomology).T),c='b')
    maxval = MorseActions[-1][-1]
    eps = 25
    plt.plot([-eps,maxval+eps],[-eps,maxval+eps],c='g')
    axes = plt.gca()
    axes.set_xlim([-eps,maxval+eps])
    axes.set_ylim([-eps,maxval+eps])
    print("\n\n---  some stats for the given instance  ---")
    print("calculating Persistent Homology took: "+str(t1-t0)+"s")
    print("with "+str(len(H)) + " many nodes and " + str(H.number_of_edges()) + " many edges, and "+str(len(MorseActions))+" many MorseActions")
    print("the nullspace of a Matrix has been calculated " + str(numberofnullspacecalcs)+" many times")
    plt.show()

def distance(p,q):
    return np.linalg.norm(np.array(p) - np.array(q))

def callback():
    #print(points)
    global canvas
    global mothernodecount 
    global vorvertices
    mothernodecount = 0
    vor = Voronoi(np.array(points))
    for p in vor.ridge_points:
        #print(p)
        if drawVoronoi:
            canvas.create_pointline(points[p[0]],points[p[1]],width=3)
        G.add_edge(p[0],p[1],weight=distance(points[p[0]],points[p[1]]))
    vorvertices = vor.vertices
    for p in vor.vertices:
        if drawVoronoi:
            canvas.create_circle(p[0],p[1],5,fill="red")
        G.add_node(nodecount + mothernodecount)
        mothernodecount += 1
    #print(vor.ridge_vertices)
    for i in range(len(vor.ridge_vertices)):
        #connect points[ridge_point[i][0,1]] to vertex[ridge_vertices[i][0,1]]
        if vor.ridge_vertices[i][0] != -1:
            if drawVoronoi:
                canvas.create_pointline(vor.vertices[vor.ridge_vertices[i][0]],points[vor.ridge_points[i][0]],width=3, fill = "red")
                canvas.create_pointline(vor.vertices[vor.ridge_vertices[i][0]],points[vor.ridge_points[i][1]],width=3, fill = "red")
            distance1 = distance(vor.vertices[vor.ridge_vertices[i][0]],points[vor.ridge_points[i][0]])
            distance2 = distance(vor.vertices[vor.ridge_vertices[i][0]],points[vor.ridge_points[i][1]])
            G.add_edge(nodecount + vor.ridge_vertices[i][0],vor.ridge_points[i][0],weight=distance1)
            G.add_edge(nodecount + vor.ridge_vertices[i][0],vor.ridge_points[i][1],weight=distance2)
        if vor.ridge_vertices[i][1] != -1:
            if drawVoronoi:
                canvas.create_pointline(vor.vertices[vor.ridge_vertices[i][1]],points[vor.ridge_points[i][0]],width=3, fill = "red")
                canvas.create_pointline(vor.vertices[vor.ridge_vertices[i][1]],points[vor.ridge_points[i][1]],width=3, fill = "red")
            distance1 = distance(vor.vertices[vor.ridge_vertices[i][1]],points[vor.ridge_points[i][0]])
            distance2 = distance(vor.vertices[vor.ridge_vertices[i][1]],points[vor.ridge_points[i][1]])
            G.add_edge(nodecount + vor.ridge_vertices[i][1],vor.ridge_points[i][0],weight=distance1)
            G.add_edge(nodecount + vor.ridge_vertices[i][1],vor.ridge_points[i][1],weight=distance2)
    #drawGraph(G)
    calculateHom2(G)


b = Button(root, text="OK", command=callback)
b.grid(row=1,column=0)

def getposition(eventorigin):
    global points
    global nodecount
    x = eventorigin.x
    y = eventorigin.y
    canvas.create_circle(x,y,5,fill="black")
    print(x,y)
    points.append([x,y])
    G.add_node(nodecount)
    nodecount += 1

root.bind("<Button 3>",getposition)

root.resizable(False, False)

root.mainloop()
