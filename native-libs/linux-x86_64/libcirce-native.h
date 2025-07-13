#ifndef ___HOME_PH_CODE_CIRCE_BE_TARGET_LIBCIRCE_NATIVE_H
#define ___HOME_PH_CODE_CIRCE_BE_TARGET_LIBCIRCE_NATIVE_H

#include <graal_isolate.h>


#if defined(__cplusplus)
extern "C" {
#endif

char* circe_build_cohort_sql(graal_isolatethread_t*, char*, char*);

char* circe_check_cohort_expression(graal_isolatethread_t*, char*);

void circe_free_string(graal_isolatethread_t*, char*);

int run_main(int argc, char** argv);

#if defined(__cplusplus)
}
#endif
#endif
