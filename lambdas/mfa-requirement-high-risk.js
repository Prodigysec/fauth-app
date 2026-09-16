function checkRequired(result, user, registration, context) {
  if (context.clientRisk.status === "HIGH") {
    result.required = true;
  }
}
