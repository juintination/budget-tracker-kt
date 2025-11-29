import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

class ExpenseTracker {

    private val br = BufferedReader(InputStreamReader(System.`in`))

    private val expenses = mutableListOf<Expense>()
    private var nextId = 1

    fun run() {
        try {
            mainLoop()
        } catch (e: Exception) {
            println("오류 발생: ${e.message}")
            e.printStackTrace()
        } finally {
            br.close()
        }
    }

    private fun printMenu() {
        println("\n===== 미니 가계부 =====")
        println("1. 지출 추가")
        println("2. 지출 목록 보기")
        println("3. 지출 총합 보기")
        println("4. 가장 많이 쓴 항목 보기")
        println("5. CSV로 저장")
        println("6. CSV에서 불러오기")
        println("0. 종료")
        print("선택 > ")
    }

    private fun mainLoop() {
        while (true) {
            printMenu()
            when (readLineSafe()) {
                "1" -> addExpense()
                "2" -> showExpenses()
                "3" -> showTotal()
                "4" -> showMax()
                "5" -> saveToCsv()
                "6" -> loadFromCsv()
                "0" -> return
                else -> println("잘못된 입력입니다.")
            }
        }
    }

    private fun readLineSafe(): String {
        val line = br.readLine() ?: throw IllegalStateException("입력을 더 이상 읽을 수 없습니다.")
        return line.trim()
    }

    private fun readInt(prompt: String): Int {
        while (true) {
            print(prompt)
            val input = readLineSafe()
            val number = input.toIntOrNull()

            if (number != null) {
                return number
            } else {
                println("숫자를 입력해주세요.")
            }
        }
    }

    private fun readStr(prompt: String): String {
        print(prompt)
        return readLineSafe()
    }

    private fun addExpense() {
        val amount = readInt("금액 입력: ")
        val memo = readStr("메모 입력: ")
        val date = readStr("날짜 입력(YYYY-MM-DD): ")

        val expense = Expense(nextId++, amount, memo, date)

        expenses.add(expense)
        println("지출이 추가되었습니다.")
    }

    private fun showExpenses() {
        if (expenses.isEmpty()) {
            println("지출 내역이 없습니다.")
            return
        }

        println("\n--- 지출 목록 ---")
        expenses.forEach { expense ->
            println("ID:${expense.id} | ${expense.amount}원 | ${expense.memo} | ${expense.date}")
        }
    }

    private fun showTotal() {
        val total = expenses.sumOf { expense -> expense.amount }
        println("총 지출: ${total}원")
    }

    private fun showMax() {
        if (expenses.isEmpty()) {
            println("지출 내역 없음")
            return
        }

        val maxExpense = expenses.maxByOrNull { expense -> expense.amount }
        if (maxExpense != null) {
            println("가장 많이 쓴 항목: ${maxExpense.amount}원 (${maxExpense.memo})")
        }
    }

    // CSV 필드 하나를 "..."로 감싸고, 내부의 따옴표는 ""로 이스케이프
    private fun escapeCsvField(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    // 한 줄의 CSV를 파싱해서 필드 리스트로 분리
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val ch = line[index]

            if (ch == '\"') {
                if (inQuotes && index + 1 < line.length && line[index + 1] == '\"') {
                    // "" -> " 로 해석
                    current.append('\"')
                    index += 1
                } else {
                    // 따옴표 열기/닫기 토글
                    inQuotes = !inQuotes
                }
            } else if (ch == ',' && !inQuotes) {
                // 따옴표 밖의 콤마는 필드 구분
                result.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }

            index += 1
        }

        // 마지막 필드 추가
        result.add(current.toString())
        return result
    }

    private fun saveToCsv() {
        val file = File("expenses.csv")

        file.printWriter().use { writer ->
            writer.println("id,amount,memo,date")
            expenses.forEach { expense ->
                val fields = listOf(
                    expense.id.toString(),
                    expense.amount.toString(),
                    expense.memo,
                    expense.date
                )

                val line = fields.joinToString(",") { field ->
                    escapeCsvField(field)
                }

                writer.println(line)
            }
        }

        println("CSV 저장 완료!")
    }

    private fun loadFromCsv() {
        val file = File("expenses.csv")

        if (!file.exists()) {
            println("CSV 파일 없음")
            return
        }

        val lines = file.readLines().drop(1)
        expenses.clear()

        lines.forEach { line ->
            if (line.isBlank()) {
                return@forEach
            }

            val columns = parseCsvLine(line)
            if (columns.size >= 4) {
                val idText = columns[0]
                val amountText = columns[1]
                val memo = columns[2]
                val date = columns[3]

                val id = idText.toInt()
                val amount = amountText.toInt()

                val expense = Expense(id, amount, memo, date)
                expenses.add(expense)
            }
        }

        val maxId = expenses.maxOfOrNull { expense -> expense.id } ?: 0
        nextId = maxId + 1

        println("CSV 불러오기 완료!")
    }
}
