# Apollon

## Introduction

This is a project by me and Vincent, under the instruction of Prof. Klein. 

The task is, to build a tool, that extracts a feature called "persistent Homology" from given picutres, and clusters them, based on this feature.

## Initial Feature Extraction

Since Persistent Homology is only really defined for a set of points, we must first extract "intereseting" points from a given picture. For this we plan on comparing multiple methods. Since this will be done on real pictures eventually, we want to look at something like the Harris-Operator, but also less sophisticated methods like a simple edge-detection. There are no exact plans yet though.

## Persistent Homology

Given a set of points in 2d-space, we must first construct a [Čech complex](https://en.wikipedia.org/wiki/%C4%8Cech_complex). This can be extracted from a Voronoi Diagram, or rather the Delauney-Triangulation, which is given by the planar dual of a Voronoi Diagram.

Now given this Cell-Complex filtration, given by the Čech complex at every nonzero epsilon, and the trivial inclusions, we now want to follow the "life cycle" of generators of the Homologies, in our case only the zeroth, and the first Homology. From this we derive a so called Persistence Diagram.

## Persistence Clustering

Now given two such Persistence Diagrams, we want to calculate some sort of distance for the two. For this we chose the [Wasserstein Distance](https://en.wikipedia.org/wiki/Wasserstein_metric), with the Bottleneck Distance beeing a special case of it, namely if p is infinity. Since we dont know a sufficiently quick algorithm to calcuate the Bottleneck Distance yet, we chose to approximate it, with the Wasserstein Distance with sufficiently large p.
