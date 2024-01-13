import java.util.HashMap
import java.util.Random
import java.util.TreeSet

const val L = 5000
const val TIME_LIMIT_MILLIS = 1500L

fun main() {
    val random = Random(42)
    val (N, M) = readln().split(" ").map { it.toInt() }
    val (si, sj) = readln().split(" ").map { it.toInt() }
    val A = List(N) { readln() }
    val t = List(M) { readln() }

    val solver = SolverImpl()
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

class SolverImpl : Solver {
    override fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>> {
        val ans = mutableListOf<Pair<Int, Int>>()
        var prev = Pair(si, sj)
        var S = ""
        val ts = TreeSet<String>(t)
        while (ts.isNotEmpty()) {
            var tt = ""
            var cost = Long.MAX_VALUE
            for (s in ts) {
                val tCost = calcCost(s, S, prev, A)
                if (tCost < cost) {
                    cost = tCost
                    tt = s
                }
            }
            ts.remove(tt)
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
        }
        return ans
    }

    private fun calcCost(
        s: String,
        S: String,
        prev: Pair<Int, Int>,
        A: List<String>
    ): Long {
        var common = 0
        for (i in 1 until Math.min(s.length, S.length)) {
            if (s.substring(i) == S.substring(S.length - i)) {
                common = i
            }
        }
        var tCost = 0L
        var pprev = prev
        for (l in common until s.length) {
            var p = Pair(-1, -1)
            var dist = Int.MAX_VALUE
            for (i in A.indices) {
                for (j in A[i].indices) {
                    if (A[i][j] == s[l] && distance(pprev, Pair(i, j)) < dist) {
                        p = Pair(i, j)
                        dist = distance(pprev, Pair(i, j))
                    }
                }
            }
            tCost += distance(pprev, p) + 1L
            pprev = p
        }
        return tCost
    }
}

class HillClimbing(val random: Random) : Solver {
    override fun solve(N: Int, M: Int, si: Int, sj: Int, A: List<String>, t: List<String>): List<Pair<Int, Int>> {
        val solver = Greedy(random)
        var ans = solver.solve(N, M, si, sj, A, t)
        var score = Judge.calcScore(N, M, si, sj, A, t, ans)
        var u = ArrayList<String>(t)

        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < TIME_LIMIT_MILLIS) {
            val i = random.nextInt(u.size)
            val j = random.nextInt(u.size)
            val tmp = u[i]
            u[i] = u[j]
            u[j] = tmp

            val newAns = solver.solve(N, M, si, sj, A, u)
            val newScore = Judge.calcScore(N, M, si, sj, A, u, newAns)
            System.err.println("$newScore $score")
            if (newScore > score) {
                ans = newAns
                score = newScore
            } else {
                val tmp = u[i]
                u[i] = u[j]
                u[j] = tmp
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