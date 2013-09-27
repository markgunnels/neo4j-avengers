(ns graph-demo.core
  (:import java.io.File
           org.apache.commons.io.FileUtils
           org.neo4j.tooling.GlobalGraphOperations
           org.neo4j.helpers.collection.IteratorUtil
           org.neo4j.kernel.Traversal
           org.neo4j.graphalgo.CommonEvaluators
           org.neo4j.graphalgo.GraphAlgoFactory
           org.neo4j.graphalgo.impl.centrality.EigenvectorCentralityPower
           org.neo4j.graphalgo.CommonEvaluators
           org.neo4j.graphdb.Direction
           org.neo4j.cypher.javacompat.ExecutionEngine)
  (:require [graphtastic.graph :as graph]))

;(graph/start! "/home/mark/temp/example-graph")

(defn avengers-assemble
  "Sets up graph."
  []
  (let [ironman (graph/create-node {:name "Iron Man" :type "hero"})
        pepper (graph/create-node {:name "Pepper Potts" :type "normal"})
        happy (graph/create-node {:name "Happy Hogan" :type "normal"})
        captainamerica (graph/create-node {:name "Captain America" :type "hero"})
        hulk (graph/create-node {:name "Hulk" :type "hero"})
        blackwidow (graph/create-node {:name "Black Widow" :type "hero"})
        thor (graph/create-node {:name "Thor" :type "hero"})
        hawkeye (graph/create-node {:name "Hawkeye" :type "hero"})]
    (println "here")
    (graph/relate ironman :knows pepper)
    (graph/relate ironman :knows happy)
    (graph/relate pepper :knows happy)
    (graph/relate ironman :knows captainamerica)
    (graph/relate ironman :knows blackwidow)
    (graph/relate ironman :knows hawkeye)
    (graph/relate ironman :knows hulk)
    (graph/relate ironman :knows thor)
    (graph/relate blackwidow :knows hawkeye)))

(defn nts
  [node]
  (.getProperty node "name"))

(defn heros
  []
  (into [] (graph/find-nodes {:type "hero"})))

(defn show-relationships
  []
  (let [operations (GlobalGraphOperations/at @graph/graphdb)
        relationships (.getAllRelationships operations)]
    (doall (map (fn [p] [(nts (.getStartNode p)) (nts (.getEndNode p))]) 
                (seq relationships)))))

(defn show-relationship-types
  []
  (let [operations (GlobalGraphOperations/at @graph/graphdb)
        relationship-types (.getAllRelationshipTypes operations)]
    (doall (map #(.name %)  (seq relationship-types)))))

(defn relationship-types
  []
  (let [operations (GlobalGraphOperations/at @graph/graphdb)
        relationship-types (.getAllRelationshipTypes operations)]
    relationship-types))

(defn path
  [start end]
  (let [d (GraphAlgoFactory/shortestPath (Traversal/expanderForAllTypes) 4)]
    (.findSinglePath d start end)))

(defn query
  [q]
 (let [ee (ExecutionEngine. @graph/graphdb)]
   (.execute ee q)))

(defn centrality
  []
  (query "start n=node(*) match n-[r]-m return n, count(r) as degree order by degree desc; "))

(defn eigenvector-centrality
  [node]
  (let [cost-evaluator (CommonEvaluators/doubleCostEvaluator "knows" 1)
        node-set (java.util.HashSet.)
        edge-set (java.util.HashSet.)
        operations (GlobalGraphOperations/at @graph/graphdb)]
    (.addAll node-set (seq (.getAllNodes operations)))
    (.addAll edge-set (seq (.getAllRelationships operations)))
    (println cost-evaluator)
    (println node-set)
    (println edge-set)
    (.getCentrality (EigenvectorCentralityPower. Direction/BOTH
                                                 cost-evaluator
                                                 node-set
                                                 edge-set
                                                 0.01)
                    node)))
