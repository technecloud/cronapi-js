var gulp = require('gulp'),
    autoprefixer = require('gulp-autoprefixer'),
    rename = require('gulp-rename'),
    notify = require('gulp-notify'),
    minify = require("gulp-babel-minify"),
    concat = require('gulp-concat');

gulp.task('minify', function () {
    return gulp.src('cronapi.js')	
        .pipe(minify({
	      mangle: {
		keepClassName: true
	      }
	    }))
        .pipe(rename('cronapi.min.js'))
        .pipe(gulp.dest('dist/'));
});

gulp.task('joinfiles', ['minify'],  function() {
  return gulp.src(['dist/cronapi.min.js', 'node_modules/@zxing/library/umd/index.min.js'])
    .pipe(concat('cronapi.min.js'))
    .pipe(gulp.dest('dist/'));
});

gulp.task('i18n', function () {
    return gulp.src('i18n/').pipe(gulp.dest('i18n/'));
});

gulp.task('build', ['minify', 'i18n', 'joinfiles']);

gulp.task('default', ['build']);