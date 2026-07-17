import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getPreferences, updatePreferences } from '@/api/preferences'
import {
  createEmptyPreferences,
  normalizePreferences,
  toPreferencesPayload,
} from '@/utils/preferences'

export const usePreferencesStore = defineStore('preferences', () => {
  const preferences = ref(createEmptyPreferences())
  const loaded = ref(false)

  const hasPrefs = computed(
    () =>
      preferences.value.allergens.length > 0 ||
      preferences.value.cuisinePreferences.length > 0 ||
      preferences.value.spicinessLevel > 0 ||
      preferences.value.tasteLevel > 0 ||
      preferences.value.proteinLevel > 0 ||
      preferences.value.fatLevel > 0 ||
      preferences.value.preferredMinPrice != null ||
      preferences.value.preferredMaxPrice != null,
  )

  async function fetchPreferences() {
    const data = await getPreferences()
    preferences.value = normalizePreferences(data)
    loaded.value = true
    return preferences.value
  }

  async function savePreferences(next) {
    const merged = {
      ...preferences.value,
      ...next,
    }
    const payload = toPreferencesPayload(merged)
    await updatePreferences(payload)
    preferences.value = normalizePreferences(payload)
    loaded.value = true
    return preferences.value
  }

  function clearPreferences() {
    preferences.value = createEmptyPreferences()
    loaded.value = false
  }

  return {
    preferences,
    loaded,
    hasPrefs,
    fetchPreferences,
    savePreferences,
    clearPreferences,
  }
})
