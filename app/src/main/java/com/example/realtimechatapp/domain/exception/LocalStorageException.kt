package com.example.realtimechatapp.domain.exception

sealed class LocalStorageException : Exception() {
    object RecordNotFoundException : LocalStorageException()
    object OutOfSpaceException : LocalStorageException()
    object LocalDataWriteException : LocalStorageException()
    object LocalDataReadException : LocalStorageException()
    // SQLite file is corrupted due to hardware/power failure (cannot read/write anymore)
    object DataCorruptedException : LocalStorageException()
    // developer logic error violating schema rules (e.g., duplicate PK or null in NOT NULL column)
    object ConstraintViolationException : LocalStorageException()
}