var gulp = require('gulp'),
    autoprefixer = require('gulp-autoprefixer'),
    uglify = require('gulp-uglify'),
    rename = require('gulp-rename'),
    notify = require('gulp-notify');
//gulp.task('default', gulp.series('minify', 'i18n'))

gulp.task('default', function () {
    return gulp.src('cronapi.js')
        .pipe(uglify())
        .pipe(rename('cronapi.min.js'))
        .pipe(gulp.dest('dist/'))
        .pipe(notify({ message: 'CronApi-JS build finished' }));
});
/*gulp.task('i18n', function() {
  return gulp.src('i18n/')
    .pipe(gulp.dest('i18n/'))
    .pipe(notify({ message: 'CronApi-JS build finished' }));
});*/