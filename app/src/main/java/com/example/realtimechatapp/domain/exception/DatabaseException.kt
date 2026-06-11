package com.example.realtimechatapp.domain.exception

sealed class DatabaseException : Exception() {
    object RecordNotFoundException : DatabaseException()
    object OutOfSpaceException : DatabaseException()
    object LocalDataWriteException : DatabaseException()
    // SQLite file is corrupted due to hardware/power failure (cannot read/write anymore)
    object DataCorruptedException : DatabaseException()
    // developer logic error violating schema rules (e.g., duplicate PK or null in NOT NULL column)
    object ConstraintViolationException : DatabaseException()

}