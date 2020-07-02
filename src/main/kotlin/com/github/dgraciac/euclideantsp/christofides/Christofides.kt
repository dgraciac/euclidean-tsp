package com.github.dgraciac.euclideantsp.christofides

import java.util.*


/**
 *
 * that contains all methods needed for Christofides' algorithm.
 *
 * @author      Bjørn Harald Olsen
 * @author      Oscar Täckström
 * @version     1.0
 */
class Christofides(private val verbose: Boolean) {

    /**
     * This is the method that starts the algorithm, and gives back the answer.
     *
     * @param x            The x coordinates of the cities.
     * @param y            The y coordinates of the cities.
     * @return          The path of the travelling salesman.
     * @since           1.0
     */
    //    public int[] solve(double[] x, double[] y){
    fun solve(weightMatrix: Array<DoubleArray>): IntArray {

//				double[][] weightMatrix=buildWeightMatrix(x,y);
        val mst = prim(weightMatrix, weightMatrix[0].size)
        val match = greadyMatch(mst, weightMatrix, weightMatrix[0].size)
        val nodes: Array<GraphNode?> = buildMultiGraph(match, mst)
        val route = getEulerCircuit(nodes)
        var sum = 0.0
        for (i in 1 until route.size) {
            sum += weightMatrix[route[i - 1]][route[i]]
        }
        sum += weightMatrix[route[0]][route[route.size - 1]]
        println("Summan: $sum")
        return route
    }

    /**
     * Builds the union of MST and MATCH, which is a multi graph
     *
     * @param nodes        Multigraph with only even degree nodes.
     * @return          Euler circuit with shortcuts
     * @since           1.0
     */
    private fun getEulerCircuit(nodes: Array<GraphNode?>): IntArray {
        val path: LinkedList<Vector<*>> = LinkedList()
        val tmpPath: Vector<Int> = Vector()
        var j = 0

        //lägg in första cykeln i path, getNextChild går djupet först och retu
        nodes[0]!!.getNextChild(nodes[0]!!.name, tmpPath, true)
        path.addAll(0, listOf(tmpPath))

        //gå igenom alla noder i vår path, om noden har fler utgående kanter så kolla cykler efter denna. stopp in cykeln på rätt plats
        while (j < path.size) {
            if (nodes[(path[j] as Int).toInt()]!!.hasMoreChilds()) {
                nodes[(path[j] as Int).toInt()]!!.getNextChild(nodes[(path[j] as Int).toInt()]!!.name, tmpPath, true)
                if (tmpPath.size > 0) {
                    //sätt ihop path och tmpPath
                    for (i in path.indices) {
                        if ((path[i] as Int).toInt() == (tmpPath.elementAt(0) as Int).toInt()) {
                            path.addAll(i, listOf(tmpPath))
                            break
                        }
                    }
                    tmpPath.clear()
                }
                j = 0
            } else j++
        }

        //hitta genvägar på Euler-turen
        val inPath = BooleanArray(nodes.size)
        val route = IntArray(nodes.size)
        j = 0
        for (i in path.indices) {
            if (!inPath[(path[i] as Int).toInt()]) {
                route[j] = (path[i] as Int).toInt()
                j++
                inPath[(path[i] as Int).toInt()] = true
            }
        }
        //if(j!=nodes.length) System.out.println("Warning! constructed route does not contain all nodes");
        return route
    }

    /**
     * Builds the union of MST and MATCH, which is a multi graph
     *
     * @param match       The "minimum" perfect match on the set of odd nodes.
     * @param mst           The minimal spanning tree
     * @return          One dimensional nodes matrix representing the multi graph
     * @since           1.0
     */
    private fun buildMultiGraph(match: Array<IntArray>, mst: IntArray): Array<GraphNode?> {
        val nodes: Array<GraphNode?> = arrayOfNulls<GraphNode>(mst.size)
        //skapa tomma noder
        for (i in mst.indices) {
            nodes[i] = GraphNode(i)
        }

        //lägg till noder och kanter från MST, symmetriska kanter!
        for (i in 1 until mst.size) {
            nodes[i]!!.addChild(nodes[mst[i]]!!)
            nodes[mst[i]]!!.addChild(nodes[i]!!)
        }

        //lägg till noder och kanter från MATCHNING, symmetriska kanter!
        for (i in match.indices) {
            nodes[match[i][0]]!!.addChild(nodes[match[i][1]]!!)
            nodes[match[i][1]]!!.addChild(nodes[match[i][0]]!!)
            if (verbose) println(match[i][0].toString() + "-" + match[i][1])
        }
        return nodes
    }

    /**
     * Builds up the weightmatrix from the coordinates. Calculates distance between all pairs.
     *
     * @param x           X-coordinates
     * @param y           Y-coordinates
     * @return          Two-dimensional weightmatrix.
     * @since           1.0
     */
    private fun buildWeightMatrix(x: DoubleArray, y: DoubleArray): Array<DoubleArray> {
        val dim = x.size
        val wt = Array(dim) { DoubleArray(dim) }
        var dist: Double
        for (u1 in 0 until dim) {
            for (u2 in 0 until dim) {
                if (u1 == u2) {
                    wt[u1][u2] = 0.0
                    wt[u2][u1] = 0.0
                    continue
                }
                dist = Math.sqrt(
                    Math.pow(x[u1] - x[u2], 2.0) +
                            Math.pow(y[u1] - y[u2], 2.0)
                )
                wt[u1][u2] = dist
                wt[u2][u1] = dist
            }
        }
        return wt
    }

    /**
     * Using Prim's algorithm to find the Minimal Spanning Tree.
     *
     * @param wt        Weightmatrix.
     * @param dim       Number of dimensions in the problem.
     * @return          The parentvector. p[i] gives the parent of node i.
     * @since           1.0
     */
    fun prim(wt: Array<DoubleArray>, dim: Int): IntArray {
        val queue: Vector<Int> = Vector()
        for (i in 0 until dim) queue.add(i)

        // Prim's algorithm
        val isInTree = BooleanArray(dim)
        val key = DoubleArray(dim) //avstånd från nod i och nod parent[i].
        val p = IntArray(dim) //parent
        for (i in 0 until dim) {
            key[i] = Int.MAX_VALUE.toDouble()
        }
        key[0] = 0.0 // root-node
        var u = 0
        var temp: Double
        var elem: Int
        do {
            isInTree[u] = true //lägg till noden i trädet
            queue.removeElement(u)
            for (v in 0 until dim) { // kan forenkles om det ikke er en komplett graf!
                if (!isInTree[v] && wt[u][v] < key[v]) {
                    p[v] = u
                    key[v] = wt[u][v]
                }
            }

            // ExtractMin, går igenom alla kvarvarande noder och tar ut den med kortast avstånd till trädet
            var mint = Double.MAX_VALUE
            for (i in queue.indices) {
                elem = queue.elementAt(i) as Int //ineffektivt
                temp = key[elem]
                if (temp < mint) {
                    u = elem
                    mint = temp
                }
            }
        } while (!queue.isEmpty())
        if (verbose) {
            print("Key-vektor: ")
            for (i in 0 until dim) {
                print(key[i].toString() + " ")
            }
            print("\n\n")
            print("Parent:     ")
            for (i in 0 until dim) {
                print(p[i].toString() + " ")
            }
            print("\n")
            var sum = 0.0
            for (g in 0 until dim) {
                sum += key[g]
            }
            println(
                """
                    
                    
                    $sum
                    """.trimIndent()
            )
        }
        return p
    }

    /**
     * Finds a match between the nodes that hava odd number of edges. Not perfect that gready, that is take the
     * shortest distance found first. Then the next shortest of the remaining i chosen.
     *
     * @param p           Parentvector. p[i] gives the parent of node i.
     * @param wt        Weightmatrix of the complete graph.
     * @param dim       Number of dimensions in the problem.
     * @return           Twodimensional matrix containing the pairs. Two columns where each row represent a pair.
     * @since           1.0
     */
    fun greadyMatch(p: IntArray, wt: Array<DoubleArray>, dim: Int): Array<IntArray> {
        val nodes: Array<Node?> = arrayOfNulls<Node>(p.size)

        //skapa en skog
        nodes[0] = Node(0, true) //roten
        for (i in 1 until p.size) {
            nodes[i] = Node(i, false)
        }

        //bygg ett träd av skogen
        for (i in p.indices) {
            if (p[i] != i) nodes[p[i]]!!.addChild(nodes[i])
        }

        //hitta udda noder
        val oddDegreeNodes = findOddDegreeNodes(nodes[0])
        val nOdd = oddDegreeNodes.size
        if (verbose) {
            println("Udda noder:")
            for (i in 0 until nOdd) print(oddDegreeNodes[i].toString() + ", ")
            println()
        }

        //försök hitta en så minimal matchning som möjligt med en girig metod
        //sortera alla kanter mellan de udda hörnen
        val edges: Array<Array<Edge?>> =
            Array(nOdd) { arrayOfNulls<Edge>(nOdd) }
        for (i in 0 until nOdd) {
            for (j in 0 until nOdd) {
                if ((oddDegreeNodes[i] as Int).toInt() != (oddDegreeNodes[j] as Int).toInt()) edges[i][j] = Edge(
                    (oddDegreeNodes[i] as Int).toInt(),
                    (oddDegreeNodes[j] as Int).toInt(),
                    wt[(oddDegreeNodes[i] as Int).toInt()][(oddDegreeNodes[j] as Int).toInt()]
                ) else edges[i][j] = Edge(
                    (oddDegreeNodes[i] as Int).toInt(),
                    (oddDegreeNodes[j] as Int).toInt(), Double.MAX_VALUE
                )
            }
            Arrays.sort(edges[i]) //sortera alla kanter från nod i
        }
        val matched = BooleanArray(dim)
        val match = Array(nOdd / 2) { IntArray(2) }

        // för varje hörn plocka ut den kortaste kanten
        // vid krock välj den kortaste av de näst kortaste.
        // antalet noder med udda gradtal alltid delbart med 2
        var k = 0
        for (i in 0 until nOdd) {
            for (j in 0 until nOdd) {
                if (matched[edges[i][j]!!.from] || matched[edges[i][j]!!.to]
                ) continue else {
                    matched[edges[i][j]!!.from] = true
                    matched[edges[i][j]!!.to] = true
                    match[k][0] = edges[i][j]!!.from
                    match[k][1] = edges[i][j]!!.to
                    k++
                }
            }
        }
        if (verbose) {
            println("Matchning")
            for (i in 0 until nOdd / 2) {
                println(match[i][0].toString() + "-" + match[i][1])
            }
        }
        return match
    }

    /**
     * Activates the treetraversing-routine that builds the path given by DFS.
     *
     * @param _root       The root which is the start node of the route.
     * @return          The route which is the order of nodes after the traversing.
     * @since           1.0
     */
    private fun buildRoute(_root: Node): ArrayList<Int> {
        val route: ArrayList<Int> = ArrayList()
        _root.visitBuildRoute(route)
        return route
    }

    /**
     * Activates the routine that finds vertexes which have odd number of edges.
     *
     * @param _root     Startnode.
     * @return          List of nodes with odd number of edges.
     * @since           1.0
     */
    private fun findOddDegreeNodes(_root: Node?): ArrayList<*> {
        val oddNodes: ArrayList<Int> = ArrayList()
        _root!!.visitFindOddDegreeNodes(oddNodes)
        return oddNodes
    }

}