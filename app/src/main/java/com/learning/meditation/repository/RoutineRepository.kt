package com.learning.meditation.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.dataclass.Routine
import com.learning.meditation.dataclass.RoutineStep
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RoutineRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private var cachedRoutines: List<Routine>? = null

    suspend fun fetchRoutines(userId: String): List<Routine> = coroutineScope {
        if (cachedRoutines != null) {
            return@coroutineScope cachedRoutines!!
        }

        try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("Routines")
                .get()
                .await()

            cachedRoutines = result.documents.mapNotNull { document ->
                document.toObject(Routine::class.java)
            }
            return@coroutineScope cachedRoutines!!
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error fetching routines", e)
            return@coroutineScope emptyList()
        }
    }

    suspend fun fetchRoutineSteps(userId: String, routineId: String): List<RoutineStep> {
        try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("Routines")
                .document(routineId)
                .collection("steps")
                .get()
                .await()

            val steps=result.documents.mapNotNull { document ->
                document.toObject(RoutineStep::class.java)
            }
            return steps.sortedBy { it.index }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error fetching routine steps", e)
            return emptyList()
        }
    }

    suspend fun addRoutine(userId: String, newRoutine: Routine, steps: List<RoutineStep>) {
        try {
            val routineRef = firestore.collection("users")
                .document(userId)
                .collection("Routines")
                .document(newRoutine.id)

            routineRef.set(newRoutine.copy(stepCount = steps.size)).await()

            addStepsToRoutine(userId, newRoutine.id, steps)

            cachedRoutines = cachedRoutines?.plus(newRoutine) ?: listOf(newRoutine)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error adding routine", e)
        }
    }

    private suspend fun addStepsToRoutine(userId: String, routineId: String, steps: List<RoutineStep>) {
        val stepsCollectionRef = firestore.collection("users")
            .document(userId)
            .collection("Routines")
            .document(routineId)
            .collection("steps")

        steps.forEach { step ->
            try {
                stepsCollectionRef.document(step.id).set(step).await()
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error adding step: ${step.title}", e)
            }
        }
    }

    suspend fun deleteRoutine(userId: String, routine: Routine) {
        try {
            val routineRef = firestore.collection("users")
                .document(userId)
                .collection("Routines")
                .document(routine.id)

            val stepsCollectionRef = routineRef.collection("steps")
            val stepsSnapshot = stepsCollectionRef.get().await()

            for (step in stepsSnapshot.documents) {
                step.reference.delete().await()
            }

            routineRef.delete().await()

            cachedRoutines = cachedRoutines?.filter { it.id != routine.id }

        } catch (e: Exception) {
            Log.e("FirestoreError", "Error deleting routine and steps", e)
        }
    }

    suspend fun editStepOfRoutine(
        userId: String,
        routineId: String,
        stepId: String,
        title: String,
        desc: String,
        duration: Int
    ) {
        try {
            val stepRef = firestore.collection("users")
                .document(userId)
                .collection("Routines")
                .document(routineId)
                .collection("steps")
                .document(stepId)

            stepRef.update(
                mapOf(
                    "title" to title,
                    "description" to desc,
                    "durationMinutes" to duration
                )
            ).await()

        } catch (e: Exception) {
            Log.e("FirestoreError", "Error editing step", e)
        }
    }
}

