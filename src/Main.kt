const val L = 5000

fun main() {

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
        for (k in t.indices) {
            for (l in t[k].indices) {
                var p = Pair(-1, -1)
                var dist = Int.MAX_VALUE
                for (i in A.indices) {
                    for (j in A[i].indices) {
                        if (A[i][j] == t[k][l] && distance(prev, Pair(i, j)) < dist) {
                            p = Pair(i, j)
                            dist = distance(prev, Pair(i, j))
                        }
                    }
                }
                ans.add(p)
                prev = p
            }
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