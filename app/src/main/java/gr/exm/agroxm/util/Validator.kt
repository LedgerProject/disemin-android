package gr.exm.agroxm.util

class Validator {

    fun validateUsername(username: String?): Boolean {
        return username.isNullOrEmpty() || !username.contains("@")
    }

    fun validatePassword(password: String?): Boolean {
        return password.isNullOrEmpty() || password.length < 6
    }

}