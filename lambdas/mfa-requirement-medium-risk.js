function checkRequired(result, user, registration, context) {
  if (context.clientRisk.status === "HIGH" || context.clientRisk.status === "MEDIUM") {
    result.required = true;
  }
}
