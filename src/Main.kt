import java.util.HashMap
import java.util.PriorityQueue
import java.util.Random
import java.util.TreeSet

const val L = 5000
const val TIME_LIMIT_MILLIS = 1200L

fun main() {
    val random = Random(42)
    val (N, M) = readln().split(" ").map { it.toInt() }
    val (si, sj) = readln().split(" ").map { it.toInt() }
    val A = List(N) { readln() }
    val t = List(M) { readln() }

    val solver = SolverImpl(random)
    val ans = solver.solve(N, M, si, sj, A, t)

    val ci = System.getenv("CI")
    if (ci != "true") {
        println(ans.joinToString("\n") { "${it.first} ${it.second}" })
    } else {
        println(Judge.calcScore(N, M, si, sj, A, t, ans))
    }
}

interface Solver {
    fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>>
}

fun distance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
    return Math.abs(a.first - b.first) + Math.abs(a.second - b.second)
}

class SolverImpl(val random: Random) : Solver {
    override fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>> {
        val ans = mutableListOf<Pair<Int, Int>>()
        var prev = Pair(si, sj)
        var S = ""
        val ts = TreeSet<String>(t)
        while (ts.isNotEmpty()) {
            var tts = ArrayList<String>()
            var cost = Long.MAX_VALUE
            for (s in ts) {
                val tCost = calcCost(s, S, prev, A)
                if (tCost < cost) {
                    cost = tCost
                    tts.clear()
                    tts.add(s)
                } else if (tCost == cost) {
                    cost = tCost
                    tts.add(s)
                }
            }
            val tt = tts[random.nextInt(tts.size)]
            ts.remove(tt)
            var common = 0
            for (i in 1 until Math.min(tt.length, S.length)) {
                if (tt.substring(0, i) == S.substring(S.length - i)) {
                    common = i
                }
            }
            val subt = tt.substring(common)
            val steps = findSteps(subt, A, prev)
            S += tt.substring(common)
            ans.addAll(steps)
            if (steps.isNotEmpty()) {
                prev = steps.last()
            }
        }
        return ans
    }

    private fun findSteps(
        subt: String,
        A: List<String>,
        prev: Pair<Int, Int>
    ): ArrayList<Pair<Int, Int>> {
        val steps = ArrayList<Pair<Int, Int>>()
        val INF = 1000000
        val dist = Array(A.size) { Array(A[0].length) { Array(subt.length + 1) { INF } }}
        val previous = Array(A.size) { Array(A[0].length) { Array(subt.length + 1) {Pair(-1, -1) } } }
        dist[prev.first][prev.second][0] = 0

        val queue = PriorityQueue<Triple<Int, Pair<Int, Int>, Int>>() { a, b -> a.first.compareTo(b.first) }
        queue.add(Triple(0, prev, 0))

        while (queue.isNotEmpty()) {
            val (d, p, idx) = queue.poll()
            if (d > dist[p.first][p.second][idx]) {
                continue
            }
            if (idx == subt.length) {
                var pp = p
                for (i in idx downTo 1) {
                    steps.add(pp)
                    pp = previous[pp.first][pp.second][i]
                }
                steps.reverse()
                return steps
            }
            for (i in A.indices) {
                for (j in A[i].indices) {
                    if (A[i][j] == subt[idx]) {
                        val nd = d + distance(p, Pair(i, j)) + 1
                        if (nd < dist[i][j][idx + 1]) {
                            dist[i][j][idx + 1] = nd
                            previous[i][j][idx + 1] = p
                            queue.add(Triple(nd, Pair(i, j), idx + 1))
                        }
                    }
                }
            }
        }
        throw IllegalStateException()
    }

    private fun calcCost(
        s: String,
        S: String,
        prev: Pair<Int, Int>,
        A: List<String>
    ): Long {
        var common = 0
        for (i in 1 until Math.min(s.length, S.length)) {
            if (s.substring(0, i) == S.substring(S.length - i)) {
                common = i
            }
        }
        var tCost = 0L
        var pprev = prev
        val steps = findSteps(s.substring(common), A, prev)
        for (p in steps) {
            tCost += distance(pprev, p) + 1L
            pprev = p
        }
        return tCost
    }
}

class HillClimbing(val random: Random) : Solver {
    override fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>> {
        val solver = SolverImpl(random)
        var ans = solver.solve(N, M, si, sj, A, t)
        var score = Judge.calcScore(N, M, si, sj, A, t, ans)
        var u = ArrayList<String>(t)

        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < TIME_LIMIT_MILLIS) {
            val newAns = solver.solve(N, M, si, sj, A, u)
            val newScore = Judge.calcScore(N, M, si, sj, A, u, newAns)
            System.err.println("$newScore $score")
            if (newScore > score) {
                ans = newAns
                score = newScore
            }
        }

        return ans
    }
}

class Greedy (val random: Random) : Solver {
    override fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>> {
        val map = HashMap<Char, ArrayList<String>>()
        for (s in t) {
            if (!map.containsKey(s[0])) {
                map[s[0]] = ArrayList()
            }
            map[s[0]]!!.add(s)
        }

        val ans = mutableListOf<Pair<Int, Int>>()
        var prev = Pair(si, sj)
        var S = ""

        var cnt = 0
        while (cnt < M) {
            var tt = ""
            if (S.isEmpty()) {
                if (map.containsKey(A[si][sj])) {
                    val index = random.nextInt(map[A[si][sj]]!!.size)
                    tt = map[A[si][sj]]!![index]
                    map[A[si][sj]]!!.removeAt(index)
                    if (map[A[si][sj]]!!.isEmpty()) {
                        map.remove(A[si][sj])
                    }
                } else {
                    val index = random.nextInt(map.keys.size)
                    val key = map.keys.toList()[index]
                    val index2 = random.nextInt(map[key]!!.size)
                    tt = map[key]!![index2]
                    map[key]!!.removeAt(index2)
                    if (map[key]!!.isEmpty()) {
                        map.remove(key)
                    }
                }
            } else {
                if (map.containsKey(S.last())) {
                    val index = random.nextInt(map[S.last()]!!.size)
                    tt = map[S.last()]!![index]
                    map[S.last()]!!.removeAt(index)
                    if (map[S.last()]!!.isEmpty()) {
                        map.remove(S.last())
                    }
                } else {
                    val index = random.nextInt(map.keys.size)
                    val key = map.keys.toList()[index]
                    val index2 = random.nextInt(map[key]!!.size)
                    tt = map[key]!![index2]
                    map[key]!!.removeAt(index2)
                    if (map[key]!!.isEmpty()) {
                        map.remove(key)
                    }
                }
            }

            if (S.contains(tt)) {
                cnt++
                continue
            }
            var common = 0
            for (i in 1 until Math.min(tt.length, S.length)) {
                if (tt.substring(i) == S.substring(S.length - i)) {
                    common = i
                }
            }
            for (l in common until tt.length) {
                var p = Pair(-1, -1)
                var dist = Int.MAX_VALUE
                for (i in A.indices) {
                    for (j in A[i].indices) {
                        if (A[i][j] == tt[l] && distance(prev, Pair(i, j)) < dist) {
                            p = Pair(i, j)
                            dist = distance(prev, Pair(i, j))
                        }
                    }
                }
                S += A[p.first][p.second]
                ans.add(p)
                prev = p
            }
            cnt++
        }

        return ans
    }
}

class Judge {
    companion object {
        fun calcScore(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>, ans: List<Pair<Int, Int>>): Long {
            if (ans.size > L) {
                return -1L
            }

            val S = buildString(ans, A)

            var cnt = 0
            for (s in t) {
                if (S.contains(s)) {
                    cnt++
                }
            }
            if (cnt != M) {
                return 1000L * (cnt + 1) / M
            }

            return calcMoveCost(si, sj, ans)
        }

        fun calcMoveCost(
            si: Int,
            sj: Int,
            ans: List<Pair<Int, Int>>
        ): Long {
            var cost = 0L
            var prev = Pair(si, sj)
            for (p in ans) {
                cost += distance(prev, p) + 1L
                prev = p
            }
            return Math.max(1001, 10000 - cost)
        }

        private fun buildString(
            ans: List<Pair<Int, Int>>,
            A: List<String>
        ): String {
            var S = ""
            for (p in ans) {
                S += A[p.first][p.second]
            }
            return S
        }
    }
}